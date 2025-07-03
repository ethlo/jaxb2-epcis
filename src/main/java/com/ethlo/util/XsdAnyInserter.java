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
        if (!Files.isDirectory(inputDir)) {
            throw new IllegalArgumentException("Input path is not a directory: " + inputDir);
        }
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        final DocumentBuilder db = dbf.newDocumentBuilder();
        final Map<String, Document> docsByUri = new HashMap<>();
        final List<Path> schemaPaths;
        try (Stream<Path> files = Files.walk(inputDir)) {
            schemaPaths = files.filter(p -> p.toString().endsWith(".xsd")).toList();
        }

        for (final Path schemaPath : schemaPaths) {
            docsByUri.put(schemaPath.toUri().toString(), db.parse(schemaPath.toFile()));
        }

        final Map<QName, XsdNode> nodesByQName = new HashMap<>();
        for (final Document doc : docsByUri.values()) {
            indexTypes(doc, nodesByQName);
        }

        for (final XsdNode node : nodesByQName.values()) {
            linkHierarchy(node, nodesByQName);
        }

        printHierarchy(nodesByQName);

        for (final XsdNode node : nodesByQName.values()) {
            final boolean isLeaf = node.children.isEmpty();
            if (isLeaf && !node.hasOrInheritsAny()) {
                modifyComplexType(node.element);
                System.out.println("Modified Leaf: " + node.qName.getLocalPart());
            }
        }

        for (final Path schemaPath : schemaPaths) {
            final Path relative = inputDir.relativize(schemaPath);
            final Path outputFile = outputDir.resolve(relative);
            Files.createDirectories(outputFile.getParent());
            final Document doc = docsByUri.get(schemaPath.toUri().toString());

            removeWhitespaceNodes(doc);

            final Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.transform(new DOMSource(doc), new StreamResult(outputFile.toFile()));
            System.out.println("Processed: " + schemaPath.getFileName());
        }
    }

    private void indexTypes(Document doc, Map<QName, XsdNode> nodesByQName) {
        final String targetNamespace = doc.getDocumentElement().getAttribute("targetNamespace");
        final NodeList complexTypes = doc.getElementsByTagNameNS(XS_NS, "complexType");
        for (int i = 0; i < complexTypes.getLength(); i++) {
            final Element typeEl = (Element) complexTypes.item(i);
            final String name = typeEl.getAttribute("name");
            if (!name.isEmpty()) {
                final QName qName = new QName(targetNamespace, name);
                final XsdNode node = new XsdNode(qName, typeEl);
                if (hasAnyInContent(typeEl)) {
                    node.hasAny = true;
                }
                nodesByQName.put(qName, node);
            }
        }
    }

    private void linkHierarchy(XsdNode node, Map<QName, XsdNode> nodesByQName) {
        final Element extension = findElementNS(node.element, "complexContent", "extension");
        if (extension != null) {
            final String base = extension.getAttribute("base");
            if (!base.isEmpty()) {
                final QName parentQName = resolveQName(base, extension);
                final XsdNode parentNode = nodesByQName.get(parentQName);
                if (parentNode != null) {
                    node.parent = parentNode;
                    parentNode.children.add(node);
                }
            }
        }
    }

    private void modifyComplexType(Element complexType) {
        if (findElementNS(complexType, "simpleContent") != null) {
            return;
        }

        Element contentParent = complexType;
        final Element complexContent = findElementNS(complexType, "complexContent");
        if (complexContent != null) {
            final Element extension = findElementNS(complexContent, "extension");
            contentParent = (extension != null) ? extension : findElementNS(complexContent, "restriction");
        }
        if (contentParent == null) {
            return;
        }

        Element contentModel = findElementNS(contentParent, "sequence");
        if (contentModel == null) {
            contentModel = findElementNS(contentParent, "choice");
        }
        if (contentModel == null) {
            contentModel = findElementNS(contentParent, "all");
        }

        if (contentModel == null) {
            contentModel = complexType.getOwnerDocument().createElementNS(XS_NS, XS_PREFIX + ":sequence");
            Node ref = findElementNS(contentParent, "attribute");
            if (ref == null) {
                ref = findElementNS(contentParent, "attributeGroup");
            }
            if (ref == null) {
                ref = findElementNS(contentParent, "anyAttribute");
            }
            if (ref != null) {
                contentParent.insertBefore(contentModel, ref);
            } else {
                contentParent.appendChild(contentModel);
            }
        }

        removeOldChild(contentModel, "any");
        final Element any = complexType.getOwnerDocument().createElementNS(XS_NS, XS_PREFIX + ":any");
        any.setAttribute("namespace", "##other");
        any.setAttribute("processContents", "lax");
        any.setAttribute("minOccurs", "0");
        any.setAttribute("maxOccurs", "unbounded");
        contentModel.appendChild(any);

        removeOldChild(contentParent, "anyAttribute");
        final Element anyAttr = contentParent.getOwnerDocument().createElementNS(XS_NS, XS_PREFIX + ":anyAttribute");
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
            final XsdNode child = node.children.get(i);
            printNode(child, prefix + (isTail ? "    " : "│   "), i == node.children.size() - 1);
        }
    }

    private void removeOldChild(Element parent, String localName) {
        final Element oldChild = getFirstChildElementNS(parent, localName);
        if (oldChild != null) {
            parent.removeChild(oldChild);
        }
    }

    private boolean hasAnyInContent(Element element) {
        if ("any".equals(element.getLocalName()) && XS_NS.equals(element.getNamespaceURI())) {
            return true;
        }
        final NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                if (hasAnyInContent((Element) children.item(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    private QName resolveQName(String prefixedName, Element context) {
        final String[] parts = prefixedName.split(":");
        final String prefix = parts.length > 1 ? parts[0] : null;
        final String localPart = parts.length > 1 ? parts[1] : prefixedName;
        final String namespace = context.lookupNamespaceURI(prefix);
        return new QName(namespace, localPart);
    }

    private void removeWhitespaceNodes(Document doc) throws XPathExpressionException {
        final XPath xPath = XPathFactory.newInstance().newXPath();
        final NodeList emptyTextNodes = (NodeList) xPath.evaluate("//text()[normalize-space()='']", doc, XPathConstants.NODESET);
        for (int i = 0; i < emptyTextNodes.getLength(); i++) {
            emptyTextNodes.item(i).getParentNode().removeChild(emptyTextNodes.item(i));
        }
    }

    private Element findElementNS(Element parent, String... path) {
        Element current = parent;
        for (final String name : path) {
            current = getFirstChildElementNS(current, name);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    private Element getFirstChildElementNS(Element parent, String localName) {
        for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                final Element el = (Element) child;
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
            if (this.hasAny) {
                return true;
            }
            return (this.parent != null) && this.parent.hasOrInheritsAny();
        }
    }
}