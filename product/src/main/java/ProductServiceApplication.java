import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author lhb
 * @version 1.0
 * @description: TODO
 * @date 2025/2/17 下午3:52
 */
@SpringBootApplication
@MapperScan("com.buka.mapper")
public class ProductServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class,args);
    }
}
