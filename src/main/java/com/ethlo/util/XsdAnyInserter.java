package com.ethlo.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Modifies a set of XSD files to make leaf types extensible.
 * It works in three passes:
 * 1. Parse & Index: All schemas are parsed and their types are indexed.
 * 2. Link Hierarchy: The indexed types are linked into a parent-child hierarchy.
 * 3. Modify Leafs: The hierarchy is traversed and only "leaf" types (those
 * that are not extended by other types) are modified.
 */
public class XsdAnyInserter {

    private static final String XS_NS = "http://www.w3.org/2001/XMLSchema";
    private static final String XS_PREFIX = "xs";

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: java XsdAnyInserter <input-folder> <output-folder>");
            System.exit(1);
        }
        new XsdAnyInserter().process(Paths.get(args[0]), Paths.get(args[1]));
    }

    private void process(Path inputDir, Path outputDir) throws Exception {
        if (!Files.isDirectory(inputDir))
            throw new IllegalArgumentException("Input path is not a directory: " + inputDir);
        if (!Files.exists(outputDir)) Files.createDirectories(outputDir);

        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();

        Map<String, Document> docsByUri = new HashMap<>();
        List<Path> schemaPaths;
        try (Stream<Path> files = Files.walk(inputDir)) {
            schemaPaths = files.filter(p -> p.toString().endsWith(".xsd")).toList();
        }

        for (Path schemaPath : schemaPaths) {
            docsByUri.put(schemaPath.toUri().toString(), db.parse(schemaPath.toFile()));
        }

        Map<QName, XsdNode> nodesByQName = new HashMap<>();
        for (Document doc : docsByUri.values()) {
            indexTypes(doc, nodesByQName);
        }

        for (XsdNode node : nodesByQName.values()) {
            linkHierarchy(node, nodesByQName);
        }

        printHierarchy(nodesByQName);

        // --- Step 3: Iterate through all nodes and modify only the leaf nodes ---
        for (XsdNode node : nodesByQName.values()) {
            boolean isLeaf = node.children.isEmpty();

            // A type is modified if it's a leaf AND it doesn't already have or inherit an 'any' property.
            if (isLeaf && !node.hasOrInheritsAny()) {
                modifyComplexType(node.element);
                System.out.println("Modified Leaf: " + node.qName.getLocalPart());
            }
        }

        // --- Step 4: Write all modified DOMs ---
        for (Path schemaPath : schemaPaths) {
            Path relative = inputDir.relativize(schemaPath);
            Path outputFile = outputDir.resolve(relative);
            Files.createDirectories(outputFile.getParent());
            Document doc = docsByUri.get(schemaPath.toUri().toString());

            removeWhitespaceNodes(doc);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.transform(new DOMSource(doc), new StreamResult(outputFile.toFile()));
            System.out.println("Processed: " + schemaPath.getFileName());
        }
    }

    private void indexTypes(Document doc, Map<QName, XsdNode> nodesByQName) {
        String targetNamespace = doc.getDocumentElement().getAttribute("targetNamespace");
        NodeList complexTypes = doc.getElementsByTagNameNS(XS_NS, "complexType");
        for (int i = 0; i < complexTypes.getLength(); i++) {
            Element typeEl = (Element) complexTypes.item(i);
            String name = typeEl.getAttribute("name");
            if (!name.isEmpty()) {
                QName qName = new QName(targetNamespace, name);
                XsdNode node = new XsdNode(qName, typeEl);
                if (hasAnyInContent(typeEl)) {
                    node.hasAny = true;
                }
                nodesByQName.put(qName, node);
            }
        }
    }

    private void linkHierarchy(XsdNode node, Map<QName, XsdNode> nodesByQName) {
        Element extension = findElementNS(node.element, "complexContent", "extension");
        if (extension != null) {
            String base = extension.getAttribute("base");
            if (!base.isEmpty()) {
                QName parentQName = resolveQName(base, extension);
                XsdNode parentNode = nodesByQName.get(parentQName);
                if (parentNode != null) {
                    node.parent = parentNode;
                    parentNode.children.add(node);
                }
            }
        }
    }

    private void modifyComplexType(Element complexType) {
        if (findElementNS(complexType, "simpleContent") != null) return;

        Element contentParent = complexType;
        Element complexContent = findElementNS(complexType, "complexContent");
        if (complexContent != null) {
            Element extension = findElementNS(complexContent, "extension");
            contentParent = (extension != null) ? extension : findElementNS(complexContent, "restriction");
        }
        if (contentParent == null) return;

        Element contentModel = findElementNS(contentParent, "sequence");
        if (contentModel == null) contentModel = findElementNS(contentParent, "choice");
        if (contentModel == null) contentModel = findElementNS(contentParent, "all");

        if (contentModel == null) {
            contentModel = complexType.getOwnerDocument().createElementNS(XS_NS, XS_PREFIX + ":sequence");
            Node ref = findElementNS(contentParent, "attribute");
            if (ref == null) ref = findElementNS(contentParent, "attributeGroup");
            if (ref == null) ref = findElementNS(contentParent, "anyAttribute");
            if (ref != null) contentParent.insertBefore(contentModel, ref);
            else contentParent.appendChild(contentModel);
        }

        removeOldChild(contentModel, "any");
        Element any = complexType.getOwnerDocument().createElementNS(XS_NS, XS_PREFIX + ":any");
        any.setAttribute("namespace", "##other");
        any.setAttribute("processContents", "lax");
        any.setAttribute("minOccurs", "0");
        any.setAttribute("maxOccurs", "unbounded");
        contentModel.appendChild(any);

        removeOldChild(contentParent, "anyAttribute");
        Element anyAttr = contentParent.getOwnerDocument().createElementNS(XS_NS, XS_PREFIX + ":anyAttribute");
        anyAttr.setAttribute("namespace", "##other");
        anyAttr.setAttribute("processContents", "lax");
        contentParent.appendChild(anyAttr);
    }

    private void printHierarchy(Map<QName, XsdNode> nodes) {
        System.out.println("\n--- Discovered Schema Hierarchy ---");
        nodes.values().stream()
                .filter(node -> node.parent == null)
                .forEach(rootNode -> printNode(rootNode, "", true));
        System.out.println("---------------------------------\n");
    }

    private void printNode(XsdNode node, String prefix, boolean isTail) {
        System.out.println(prefix + (isTail ? "└── " : "├── ") + node.qName.getLocalPart() + " [hasAny=" + node.hasAny + "]");
        for (int i = 0; i < node.children.size(); i++) {
            XsdNode child = node.children.get(i);
            printNode(child, prefix + (isTail ? "    " : "│   "), i == node.children.size() - 1);
        }
    }

    private void removeOldChild(Element parent, String localName) {
        Element oldChild = getFirstChildElementNS(parent, localName);
        if (oldChild != null) parent.removeChild(oldChild);
    }

    private boolean hasAnyInContent(Element element) {
        if ("any".equals(element.getLocalName()) && XS_NS.equals(element.getNamespaceURI())) return true;
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                if (hasAnyInContent((Element) children.item(i))) return true;
            }
        }
        return false;
    }

    private QName resolveQName(String prefixedName, Element context) {
        String[] parts = prefixedName.split(":");
        String prefix = parts.length > 1 ? parts[0] : null;
        String localPart = parts.length > 1 ? parts[1] : prefixedName;
        String namespace = context.lookupNamespaceURI(prefix);
        return new QName(namespace, localPart);
    }

    private void removeWhitespaceNodes(Document doc) throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList emptyTextNodes = (NodeList) xPath.evaluate("//text()[normalize-space()='']", doc, XPathConstants.NODESET);
        for (int i = 0; i < emptyTextNodes.getLength(); i++) {
            emptyTextNodes.item(i).getParentNode().removeChild(emptyTextNodes.item(i));
        }
    }

    private Element findElementNS(Element parent, String... path) {
        Element current = parent;
        for (String name : path) {
            current = getFirstChildElementNS(current, name);
            if (current == null) return null;
        }
        return current;
    }

    private Element getFirstChildElementNS(Element parent, String localName) {
        for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) child;
                if (localName.equals(el.getLocalName()) && XS_NS.equals(el.getNamespaceURI())) {
                    return el;
                }
            }
        }
        return null;
    }

    private static class XsdNode {
        final QName qName;
        final Element element;
        final List<XsdNode> children = new ArrayList<>();
        XsdNode parent = null;
        boolean hasAny = false;

        public XsdNode(QName qName, Element element) {
            this.qName = qName;
            this.element = element;
        }

        public boolean hasOrInheritsAny() {
            if (this.hasAny) return true;
            return (this.parent != null) && this.parent.hasOrInheritsAny();
        }
    }
}