package com.yff.aicodemother.ai.core;

import com.yff.aicodemother.ai.model.enums.CodeGenTypeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.util.List;


@SpringBootTest
class AiCodeGeneratorFaadeTest {

    @Autowired
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;


    @Test
    void generateVueProjectCodeStream() {

        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream("简单的任务记录网站", CodeGenTypeEnum.VUE_PROJECT, 1L);
        List<String> result = codeStream.collectList().block();

        Assertions.assertNotNull(result);
        String join = String.join("", result);
        Assertions.assertNotNull(join);



    }


}