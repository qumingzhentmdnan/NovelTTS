package com.fjut.noveltts;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.regex.Pattern;

@SpringBootTest
class NovelTtsApplicationTests {

    @Test
    void contextLoads() {
        Pattern pattern = Pattern.compile("“*?[，；。！？……]”");
        String s = "“我没有足够的证据，除了自己的“认知”之外，我没有任何办法证明那场大火的存在，我也不确定是什么力量在操纵这件事情，我只是在怀疑。”";
        pattern.matcher(s).results().forEach(System.out::println);
    }

}
