package eu.comsode.unifiedviews.plugins.extractor.sksoiresults;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;

/**
 * Main data processing unit class.
 */
@DPU.AsExtractor
public class SkSOIResults extends AbstractDpu<SkSOIResultsConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(SkSOIResults.class);

    private static final String INPUT_URL = "http://www.soi.sk/sk/Pravoplatne-rozhodnutia/Prvostupnove.soi";

    @DataUnit.AsOutput(name = "filesDownloadConfiguration")
    public WritableRDFDataUnit configurationOutput;

    public SkSOIResults() {
        super(SkSOIResultsVaadinDialog.class, ConfigHistory.noHistory(SkSOIResultsConfig_V1.class));
    }

    @Override
    protected void innerExecute() throws DPUException {
        Set<String> listOfRTFs = new HashSet<String>();
        try {
            Document doc = null;
            Document docInner = null;

            doc = Jsoup.connect(INPUT_URL).userAgent("Mozilla").get();

            Element content = doc.select("div.p-box").first();
            Element list = content.select("ul").first();
            Elements links = list.select("a[href]");

            Set<String> listOfLinks = new HashSet<String>();
            int count = 0;
            for (Element link : links) {
                count = listOfLinks.size();
                listOfLinks.add(link.absUrl("href"));
                if (count == listOfLinks.size()) {
                    continue;
                }
                docInner = Jsoup.connect(link.absUrl("href")).userAgent("Mozilla").get();
                Element innerContent = docInner.select("div.p-box").first();
                Elements innerList = innerContent.select("li");
                Elements innerLinks = innerList.select("a[href]");
                for (Element innerLink : innerLinks) {
                    listOfRTFs.add(innerLink.absUrl("href"));
                }
            }

        } catch (IOException ex) {
            throw ContextUtils.dpuException(ctx, ex, "SkSOIResults.execute.exception");
        }

        StringBuilder sb = new StringBuilder();
        int index = 0;
        for (String downloadURI : listOfRTFs) {
            sb.append("<http://localhost/resource/file/").append(index).append(">\n   ");
            sb.append("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://unifiedviews.eu/ontology/dpu/filesDownload/File>;\n   ");
            sb.append("<http://unifiedviews.eu/ontology/dpu/filesDownload/file/uri> ").append('"').append(downloadURI).append('"').append(";\n   ");
            sb.append("<http://unifiedviews.eu/ontology/dpu/filesDownload/file/fileName> ").append('"').append(URI.create(downloadURI).getPath()).append('"').append(".\n\n");
            index++;
        }
        sb.append("<http://localhost/resource/config>\n   ");
        sb.append("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://unifiedviews.eu/ontology/dpu/filesDownload/Config>;\n");
        for (int i = 0; i < index - 1; i++) {
            sb.append("   <http://unifiedviews.eu/ontology/dpu/filesDownload/hasFile> <http://localhost/resource/file/").append(i).append(">;\n");
        }
        sb.append("   <http://unifiedviews.eu/ontology/dpu/filesDownload/hasFile> <http://localhost/resource/file/").append(index - 1).append(">.\n");

        RepositoryConnection connection = null;
        try {
            connection = configurationOutput.getConnection();
            org.openrdf.model.URI graph = configurationOutput.addNewDataGraph("configurationFilesDownload");
            connection.add(new StringReader(sb.toString()), "http://localhost", RDFFormat.TURTLE, graph);
        } catch (DataUnitException | RDFParseException | RepositoryException | IOException ex) {
            throw ContextUtils.dpuException(ctx, ex, "SkSOIResults.execute.exception");
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {
                    LOG.warn("Error in close", ex);
                }
            }
        }
    }

    public static FileOutputStream openOutputStream(File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (file.canWrite() == false) {
                throw new IOException("File '" + file + "' cannot be written to");
            }
        } else {
            File parent = file.getParentFile();
            if (parent != null) {
                if (!parent.mkdirs() && !parent.isDirectory()) {
                    throw new IOException("Directory '" + parent + "' could not be created");
                }
            }
        }
        return new FileOutputStream(file);
    }
}
