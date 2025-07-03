package com.ethlo.jaxb;

import com.gs1.epcis.EPCISDocumentType;
import com.gs1.epcis.ObjectEventType;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Unmarshaller;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unchecked")
public class ObjectEventCustomExtensionTest {

    @Test
    void shouldContainCustomElementInAny() throws Exception {
        String xml = """
                <epcis:EPCISDocument xmlns:epcis="urn:epcglobal:epcis:xsd:1"
                                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                                     xmlns:cbv="urn:epcglobal:cbv:mda"
                                     xmlns:ext="http://example.com/epcis/ext"
                                     schemaVersion="1.2" creationDate="2024-01-01T00:00:00Z">
                    <EPCISBody>
                        <EventList>
                            <ObjectEvent>
                                <eventTime>2024-01-01T12:00:00Z</eventTime>
                                <eventTimeZoneOffset>+01:00</eventTimeZoneOffset>
                                <epcList>
                                    <epc>urn:epc:id:sgtin:123456.789.1234</epc>
                                </epcList>
                                <action>OBSERVE</action>
                                <bizStep>urn:epcglobal:cbv:bizstep:shipping</bizStep>
                                <ext:customData customAttr="customValue">HelloWorld</ext:customData>
                            </ObjectEvent>
                        </EventList>
                    </EPCISBody>
                </epcis:EPCISDocument>
                """;

        final JAXBContext ctx = JAXBContext.newInstance(EPCISDocumentType.class);
        final Unmarshaller unmarshaller = ctx.createUnmarshaller();

        final JAXBElement<EPCISDocumentType> doc = (JAXBElement<EPCISDocumentType>)
                unmarshaller.unmarshal(new StringReader(xml));

        final JAXBElement<ObjectEventType> event = ((JAXBElement<ObjectEventType>)
                doc.getValue()
                        .getEPCISBody()
                        .getEventList()
                        .getObjectEventOrAggregationEventOrQuantityEvent().get(0));

        final List<Object> anyElements = event.getValue().getAny();

        // Check custom element is present
        assertThat(anyElements)
                .anyMatch(el ->
                        el instanceof Element &&
                        ((Element) el).getLocalName().equals("customData") &&
                        ((Element) el).getTextContent().equals("HelloWorld")
                );

        // Check custom attribute on custom element
        anyElements.stream()
                .filter(el -> el instanceof Element && ((Element) el).getLocalName().equals("customData"))
                .map(el -> (Element) el)
                .findFirst()
                .ifPresent(el -> {
                    String attr = el.getAttribute("customAttr");
                    assertThat(attr).isEqualTo("customValue");
                });
    }
}
