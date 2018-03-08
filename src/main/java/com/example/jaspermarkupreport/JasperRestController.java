package com.example.jaspermarkupreport;

import net.sf.jasperreports.engine.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
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

        Charset utf8Charset = Charset.forName("UTF-8");
        String rtfText = StreamUtils.copyToString(rtf.getInputStream(), utf8Charset);
        String htmlText = StreamUtils.copyToString(html.getInputStream(), utf8Charset);

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
