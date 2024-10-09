package edu.fudan.se.sctap_lowcode_tool;

import edu.fudan.se.sctap_lowcode_tool.service.impl.ImportServiceImpl;
import edu.fudan.se.sctap_lowcode_tool.utils.import_utils.MetaBFSIterator;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.FileInputStream;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class FileImportTest {

    private static final Logger log = LoggerFactory.getLogger(FileImportTest.class);

    @Autowired
    private ImportServiceImpl importService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void importServiceTest() {
        Path base = Path.of("src/main/resources/jsonFolder3");
        String projectName = "test" + java.time.LocalDateTime.now().
                format(java.time.format.DateTimeFormatter.ofPattern("MMdd-HHmm"));
        try {
            MetaBFSIterator iterator = MetaBFSIterator.usingIndex(base);
            importService.importMetaRecursively(iterator, projectName);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Test
    public void importApiTest() throws Exception {

        String filePath = "src/main/resources/jsonFolder3.zip";
        String projectName = "test" + java.time.LocalDateTime.now().
                format(java.time.format.DateTimeFormatter.ofPattern("MMdd-HHmm"));

        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "jsonFolder3.zip",
                "application/zip",
                new FileInputStream(filePath)
        );

        mockMvc.perform(
                multipart("/api/import/upload")
                .file(mockFile)
                        .param("projectName", projectName)
                ).andExpect(status().isOk());
    }


}
