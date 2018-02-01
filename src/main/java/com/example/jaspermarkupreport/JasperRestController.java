package com.example.jaspermarkupreport;

import net.sf.jasperreports.engine.*;
import org.apache.tools.ant.util.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

@RestController
public class JasperRestController {

    @Value("classpath:reports/MarkupReport.jrxml")
    private Resource markupReport;

    @Value("classpath:reports/rtf.txt")
    private Resource rtf;

    @Value("classpath:reports/html.txt")
    private Resource html;


    @RequestMapping(method = RequestMethod.GET)
    public void preview(HttpServletResponse response) throws Exception {

        String rtfText = FileUtils.readFully(new FileReader(rtf.getFile()));
        String htmlText = FileUtils.readFully(new FileReader(html.getFile()));

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("RtfText", rtfText);
        parameters.put("HtmlText", htmlText);

        JasperReport jasperMasterReport = JasperCompileManager
                .compileReport(markupReport.getInputStream());
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperMasterReport,
                parameters,
                new JREmptyDataSource());

        OutputStream outputStream = new ByteArrayOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);

        response.setHeader("Content-Disposition", "inline; filename=\"MarkupReport.pdf\"");
        byte[] bytes = ((ByteArrayOutputStream) outputStream).toByteArray();
        response.setContentType("application/pdf");
        response.setContentLength(bytes.length);
        FileCopyUtils.copy(new ByteArrayInputStream(bytes), response.getOutputStream());
    }

}
