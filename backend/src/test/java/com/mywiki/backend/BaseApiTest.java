  package com.mywiki.backend;

  import org.springframework.beans.factory.annotation.Autowired;
  import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
  import org.springframework.boot.test.context.SpringBootTest;
  import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
  import org.springframework.test.web.servlet.MockMvc;
  import org.testcontainers.containers.MySQLContainer;

  @SpringBootTest
  @AutoConfigureMockMvc
  public abstract class BaseApiTest {

      // 单例容器：整个测试 JVM 只启动一次，不随测试类销毁，避免上下文缓存指向死容器
      @ServiceConnection
      static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

      static {
          mysql.start();
      }

      @Autowired
      protected MockMvc mockMvc;
  }