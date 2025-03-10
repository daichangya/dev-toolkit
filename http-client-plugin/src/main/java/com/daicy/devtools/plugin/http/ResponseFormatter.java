package com.daicy.devtools.plugin.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;

public class ResponseFormatter {
    private final ObjectMapper objectMapper;

    public ResponseFormatter() {
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    public String format(String content, String contentType) {
        try {
            if (contentType.contains("json")) {
                return formatJson(content);
            } else if (contentType.contains("xml")) {
                return formatXml(content);
            } else if (contentType.contains("html")) {
                return formatHtml(content);
            }
        } catch (Exception e) {
            // 如果格式化失败，返回原始内容
            System.err.println("格式化失败: " + e.getMessage());
        }
        return content;
    }

    private String formatJson(String json) throws Exception {
        Object jsonObj = objectMapper.readValue(json, Object.class);
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObj);
    }

    private String formatXml(String xml) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        StreamSource source = new StreamSource(new StringReader(xml));
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        transformer.transform(source, result);

        return writer.toString();
    }

    private String formatHtml(String html) {
        try {
            Document doc = Jsoup.parse(html);
            // 设置漂亮打印的缩进为2个空格
            doc.outputSettings().indentAmount(2).prettyPrint(true);
            return doc.outerHtml();
        } catch (Exception e) {
            System.err.println("HTML格式化失败: " + e.getMessage());
            return html;
        }
    }

    public String getPreviewContent(String content, String contentType) {
        String baseStyle = "<style>\n" +
                          "  body { font-family: Arial, sans-serif; margin: 0; padding: 20px; line-height: 1.6; color: #333; }\n" +
                          "  pre { background-color: #f8f9fa; border: 1px solid #e9ecef; border-radius: 4px; padding: 15px; " +
                          "        margin: 10px 0; white-space: pre-wrap; font-family: 'Consolas', 'Monaco', monospace; font-size: 14px; }\n" +
                          "  .json { color: #000; }\n" +
                          "  .json .string { color: #22863a; }\n" +
                          "  .json .number { color: #005cc5; }\n" +
                          "  .json .boolean { color: #005cc5; }\n" +
                          "  .json .null { color: #005cc5; }\n" +
                          "  .xml { color: #000; }\n" +
                          "  .xml .tag { color: #22863a; }\n" +
                          "  .xml .attr-name { color: #6f42c1; }\n" +
                          "  .xml .attr-value { color: #032f62; }\n" +
                          "</style>\n" +
                          "<script>\n" +
                          "function highlightJson(json) {\n" +
                          "  return json.replace(/\"(\\\\u[a-zA-Z0-9]{4}|\\\\[^u]|[^\\\\\"])*\"(\\s*:)?|\\b(true|false|null)\\b|-?\\d+(?:\\.\\d*)?(?:[eE][+-]?\\d+)?/g, function(match) {\n" +
                          "    let cls = 'number';\n" +
                          "    if (/^\"/.test(match)) {\n" +
                          "      if (/:$/.test(match)) {\n" +
                          "        cls = 'key';\n" +
                          "      } else {\n" +
                          "        cls = 'string';\n" +
                          "      }\n" +
                          "    } else if (/true|false/.test(match)) {\n" +
                          "      cls = 'boolean';\n" +
                          "    } else if (/null/.test(match)) {\n" +
                          "      cls = 'null';\n" +
                          "    }\n" +
                          "    return '<span class=\"' + cls + '\">' + match + '</span>';\n" +
                          "  });\n" +
                          "}\n" +
                          "\n" +
                          "function highlightXml(xml) {\n" +
                          "  return xml.replace(/(&lt;[\\w\\s=\".\\-:]+\\/?)(&gt;)|(&lt;\\/[\\w\\s=\".\\-:]+)(&gt;)|([\\w\\-.]+=\"[^\"]*\")|([\\w\\-.]+)/g,\n" +
                          "    function(match, openTag, openEnd, closeTag, closeEnd, attr, text) {\n" +
                          "      if (openTag && openEnd) {\n" +
                          "        return '<span class=\"tag\">' + openTag + openEnd + '</span>';\n" +
                          "      } else if (closeTag && closeEnd) {\n" +
                          "        return '<span class=\"tag\">' + closeTag + closeEnd + '</span>';\n" +
                          "      } else if (attr) {\n" +
                          "        let parts = attr.split('=\"');\n" +
                          "        return '<span class=\"attr-name\">' + parts[0] + '</span>=<span class=\"attr-value\">\"' + parts[1] + '</span>';\n" +
                          "      }\n" +
                          "      return text || match;\n" +
                          "    }\n" +
                          "  );\n" +
                          "}\n" +
                          "\n" +
                          "window.onload = function() {\n" +
                          "  let pre = document.querySelector('pre');\n" +
                          "  if (pre) {\n" +
                          "    if (pre.classList.contains('json')) {\n" +
                          "      pre.innerHTML = highlightJson(pre.innerHTML);\n" +
                          "    } else if (pre.classList.contains('xml')) {\n" +
                          "      pre.innerHTML = highlightXml(pre.innerHTML);\n" +
                          "    }\n" +
                          "  }\n" +
                          "};\n" +
                          "</script>";

        String docType = "<!DOCTYPE html>\n";
        String head = "<html><head><meta charset='UTF-8'>" + baseStyle + "</head><body>";
        String tail = "</body></html>";

        if (contentType.contains("html")) {
            // 如果是HTML内容，尝试清理和格式化，但保留原始结构
            try {
                Document doc = Jsoup.parse(content);
                doc.outputSettings().prettyPrint(true).indentAmount(2);
                // 注入我们的样式，但保留原始内容的样式
                return docType + "<html><head><meta charset='UTF-8'>" + 
                       baseStyle + 
                       doc.head().html() + 
                       "</head><body>" + 
                       doc.body().html() + 
                       "</body></html>";
            } catch (Exception e) {
                // 如果解析失败，回退到简单显示
                return docType + head + "<pre>" + escapeHtml(content) + "</pre>" + tail;
            }
        } else if (contentType.contains("json")) {
            return docType + head + "<pre class='json'>" + escapeHtml(content) + "</pre>" + tail;
        } else if (contentType.contains("xml")) {
            return docType + head + "<pre class='xml'>" + escapeHtml(content) + "</pre>" + tail;
        } else {
            return docType + head + "<pre>" + escapeHtml(content) + "</pre>" + tail;
        }
    }

    private String escapeHtml(String content) {
        return content.replace("&", "&amp;")
                     .replace("<", "&lt;")
                     .replace(">", "&gt;")
                     .replace("\"", "&quot;")
                     .replace("'", "&#39;");
    }
} 