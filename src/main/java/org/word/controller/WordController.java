package org.word.controller;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.word.model.Table;
import org.word.service.WordService;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by XiuYin.Cui on 2018/1/11.
 */
@Controller
public class WordController {

  @Autowired
  private WordService tableService;

  @Autowired
  private RestTemplate restTemplate;

  @Value("${server.port}")
  private Integer port;

  @Value("${swagger.url}")
  private String swaggerUrl;

  private static Set<String> titleSize = new HashSet<>();

  /**
   * 将 swagger 文档转换成 html 文档，可通过在网页上右键另存为 xxx.doc 的方式转换为 word 文档
   *
   * @param model
   * @return
   */
  @Deprecated
  @RequestMapping("/toWord")
  public String getWord(Model model) {
    List<Table> tables = tableService.tableList();
    tables = getFinalList(tables);
    model.addAttribute("tables", tables);
    return "word";
  }

  private List<Table> getFinalList(List<Table> tables) {
    Collections.sort(tables);
    for (Table item : tables) {
      if(titleSize.contains(item.getTitle())){
        item.setTitle("");
      }else {
        titleSize.add(item.getTitle());
      }
    }
    return tables;
  }

  /**
   * 将 swagger 文档一键下载为 doc 文档
   * http://localhost:8082/downloadWord?fileName=
   *
   * @param response
   */
  @RequestMapping("/downloadWord")
  public void word(HttpServletResponse response,
                   @RequestParam String fileName) {
    if (StringUtils.isEmpty(fileName)) {
      return;
    }
    ResponseEntity<String> forEntity = restTemplate.getForEntity("http://localhost:" + port + "/toWord?url=" + swaggerUrl, String.class);
    String html = JSONObject.toJSONString(forEntity);
    response.setContentType("application/octet-stream;charset=utf-8");
    response.setCharacterEncoding("utf-8");
    fileName = fileName + ".doc";
    try (BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream())) {
      response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
      byte[] bytes = forEntity.getBody().getBytes();
      bos.write(bytes, 0, bytes.length);
      bos.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

}
