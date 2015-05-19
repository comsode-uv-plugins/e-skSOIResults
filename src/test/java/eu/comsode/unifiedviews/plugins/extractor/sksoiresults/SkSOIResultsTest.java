package eu.comsode.unifiedviews.plugins.extractor.sksoiresults;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

import org.junit.Test;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;

import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.helpers.dataunit.rdf.RDFHelper;
import eu.unifiedviews.helpers.dpu.test.config.ConfigurationBuilder;

public class SkSOIResultsTest {
    @Test
    public void testSmallFile() throws Exception {
        SkSOIResultsConfig_V1 config = new SkSOIResultsConfig_V1();

        // Prepare DPU.
        SkSOIResults dpu = new SkSOIResults();
        dpu.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());

        // Prepare test environment.
        TestEnvironment environment = new TestEnvironment();

        // Prepare data unit.
        WritableRDFDataUnit configurationOutput = environment.createRdfOutput("filesDownloadConfiguration", false);
        try {
            // Run.
            environment.run(dpu);

            // Get file iterator.
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            RepositoryConnection con = configurationOutput.getConnection();
            con.export(Rio.createWriter(RDFFormat.TURTLE, bos), RDFHelper.getGraphsURIArray(configurationOutput));
            String realOutput = new String(bos.toByteArray(), Charset.forName("UTF-8"));
            System.out.print(realOutput);

//            byte[] outputContent = FileUtils.readFileToByteArray(new File(new URI(entry.getFileURIString())));
//            byte[] expectedContent = IOUtils.toByteArray(Thread.currentThread().getContextClassLoader().getResourceAsStream("tt062015.rtf"));

//            Assert.assertArrayEquals(expectedContent, outputContent);
        } finally {
            // Release resources.
            environment.release();
        }
    }
}
