package edu.fudan.se.sctap_lowcode_tool;

import edu.fudan.se.sctap_lowcode_tool.repository.CyberResourceRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Transactional
public class ResourceRepoTest {

    @Autowired
    private CyberResourceRepository cyberResourceRepository;

    @Test
    public void test() {
        System.out.println(cyberResourceRepository.findByProjectInfoProjectId(1));
    }

}
