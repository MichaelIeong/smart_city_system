package edu.fudan.se.sctap_lowcode_tool;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.FileInputStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class FileImportTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void importApiTest() throws Exception {

        String filePath = "src/main/resources/ParkingLot.zip";
        String projectName = "test" + java.time.LocalDateTime.now().
                format(java.time.format.DateTimeFormatter.ofPattern("MMdd-HHmm"));

        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.zip",
                "application/zip",
                new FileInputStream(filePath)
        );

        mockMvc.perform(
                multipart("/api/projects/import")
                .file(mockFile)
                        .param("projectName", projectName)
        ).andExpect(status().isOk());
    }

}
