package com.ethlo.jaxb;

import com.gs1.epcis.AggregationEventType;
import com.gs1.epcis.EPCISDocumentType;
import com.gs1.epcis.QuantityElementType;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Unmarshaller;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AggregationEventCustomFieldsTypedTest {

    @Test
    void shouldExtractCustomFieldsWithProperTypes() throws Exception {
        final InputStream is = getClass().getClassLoader().getResourceAsStream("example-aggregation-event.xml");
        assertThat(is).isNotNull();

        final JAXBContext ctx = JAXBContext.newInstance(EPCISDocumentType.class);
        final Unmarshaller unmarshaller = ctx.createUnmarshaller();

        final JAXBElement<EPCISDocumentType> doc = (JAXBElement<EPCISDocumentType>) unmarshaller.unmarshal(is);

        final AggregationEventType aggregationEvent = doc.getValue()
                .getEPCISBody()
                .getEventList()
                .getObjectEventOrAggregationEventOrQuantityEvent()
                .stream()
                .filter(e -> e instanceof JAXBElement<?> je && je.getDeclaredType().equals(AggregationEventType.class))
                .map(e -> ((JAXBElement<AggregationEventType>) e).getValue())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No AggregationEvent found"));

        // Check example:ArticleAttrExpiryDate inside extension -> childQuantityList -> quantityElement
        final List<QuantityElementType> quantityElements = aggregationEvent.getExtension()
                .getChildQuantityList()
                .getQuantityElement();

        final Element expiryDateElement = quantityElements.stream()
                .map(QuantityElementType::getAny)
                .flatMap(List::stream)
                .filter(o -> o instanceof Element)
                .map(o -> (Element) o)
                .findFirst().orElseThrow();
        assertThat(expiryDateElement.getTagName()).isEqualTo("example:ArticleAttrExpiryDate");
        assertThat(expiryDateElement.getTextContent()).isEqualTo("2024-03-13T09:15:14.000Z");

        final String attrWeight = aggregationEvent.getAny().stream()
                .filter(Element.class::isInstance)
                .map(Element.class::cast)
                .filter(el -> "example:AttrWeigthKGM".equals(el.getTagName()))
                .map(Element::getTextContent)
                .findFirst()
                .orElse(null);

        final String attrHeight = aggregationEvent.getAny().stream()
                .filter(Element.class::isInstance)
                .map(Element.class::cast)
                .filter(el -> "example:AttrHeightCMT".equals(el.getTagName()))
                .map(Element::getTextContent)
                .findFirst()
                .orElse(null);

        assertThat(attrWeight).isEqualTo("10.050");
        assertThat(attrHeight).isEqualTo("25");
    }
}
