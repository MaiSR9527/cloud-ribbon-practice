package ribbon.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author MaiShuRen
 * @site https://www.maishuren.top
 * @since 2020-07-05
 */
@RestController
public class OrderController {

    @Value("${server.port}")
    private Integer port;

    @Value("${spring.application.name}")
    private String name;

    @GetMapping("/test")
    public String add() {
        return "this service name is " + name + " and port is " + port;
    }

}
