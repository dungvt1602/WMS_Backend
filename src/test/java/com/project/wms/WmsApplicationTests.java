package com.project.wms;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Tạm thời vô hiệu hóa để không cản trở quá trình commit khi chưa bật Docker (Redis/Kafka)")
class WmsApplicationTests {

    @Test
    void contextLoads() {
    }

}
