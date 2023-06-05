package companieshouse.gov.uk.githubapi.service.mvn;

import static companieshouse.gov.uk.githubapi.util.XmlUtils.loadDependencySpec;
import static companieshouse.gov.uk.githubapi.util.XmlUtils.getElementsByTagName;
import static companieshouse.gov.uk.githubapi.util.XmlUtils.getNodeStream;
import static companieshouse.gov.uk.githubapi.util.NameUtils.normaliseName;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class DependencyManagementParseRule implements ParseRule {

    @Override
    public Map<String, String> run(final Document xmlDocument) {
        
        final Element root = xmlDocument.getDocumentElement();
        final Optional<Element> dependencyManagement = getElementsByTagName(root, "dependencyManagement");

        return dependencyManagement.map(
            element -> {
                final NodeList children = element.getElementsByTagName("dependencies");
                final Element dependencies = (Element) children.item(0);

                return getNodeStream(dependencies.getChildNodes(), "dependency")
                    .map(node -> loadDependencySpec((Element) node))
                    .filter(spec -> spec.version().isPresent())
                    .map(spec -> Map.of(normaliseName(spec.artifactId()), spec.version().get()))
                    .flatMap(map -> map.entrySet().stream())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            }
        ).orElseGet(() -> Map.of());
    }
}