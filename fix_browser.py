import re

with open('lab4/src/BrowserTab.java', 'r') as f:
    content = f.read()

# Make home page super colorful
new_home = """private String generateHomePageHtml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='text-align: center; padding: 40px; font-family: \\"Segoe UI\\", sans-serif; background-color: #0f172a; color: #f8fafc;'>");
        sb.append("<div style='margin-bottom: 30px;'>");
        sb.append("<h1 style='color: #38bdf8; font-size: 42px; margin: 0;'>🚀 Nexus Browser</h1>");
        sb.append("<p style='color: #94a3b8; font-size: 16px; font-style: italic;'>Exploratorul tău web ultra-rapid</p>");
        sb.append("</div>");
        
        sb.append("<div style='margin: 30px auto; max-width: 650px; text-align: left; background-color: #1e293b; padding: 20px; border-radius: 12px; border: 1px solid #334155;'>");
        sb.append("<h2 style='color: #a78bfa; margin-top: 0; border-bottom: 1px solid #334155; padding-bottom: 10px;'>🌟 Linkuri Rapide</h2>");
        sb.append("<p style='font-size: 15px;'>");
        sb.append("• <a href='https://html.duckduckgo.com' style='color: #f472b6; text-decoration: none;'>DuckDuckGo HTML</a><br><br>");
        sb.append("• <a href='https://ro.wikipedia.org' style='color: #34d399; text-decoration: none;'>Wikipedia Română</a><br><br>");
        sb.append("• <a href='https://example.com' style='color: #fbbf24; text-decoration: none;'>Example Domain (Simplu)</a><br><br>");
        sb.append("• <a href='https://www.google.com' style='color: #60a5fa; text-decoration: none;'>Google Search</a>");
        sb.append("</p></div>");

        sb.append("</body></html>");
        return sb.toString();
    }"""

content = re.sub(r'private String generateHomePageHtml\(\) \{.*?(?=private String generateHistoryPageHtml\(\) \{)', new_home + '\n\n    ', content, flags=re.DOTALL)

# Make search results colorful
new_search = """private PageLoadResult buildGlobalSearchResults(String query) {
            String encoded = query;
            try { encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.name()); } catch (Exception e) {}
            
            List<String> wikiUrls = new ArrayList<>();
            List<String> relatedLinks = new ArrayList<>();
            String abstractText = "";
            String abstractUrl = "";
            
            try {
                URL wikiUrl = new URI("https://ro.wikipedia.org/w/api.php?action=opensearch&search=" + encoded + "&limit=6&format=json").toURL();
                HttpURLConnection conn = (HttpURLConnection) wikiUrl.openConnection();
                conn.setConnectTimeout(6000);
                if (conn.getResponseCode() == 200) {
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                        StringBuilder sb = new StringBuilder(); String l; while ((l = in.readLine()) != null) sb.append(l);
                        String raw = sb.toString();
                        Matcher mWiki = Pattern.compile("\\"(https://[^\\"]+wikipedia\\\\.org/wiki/[^\\"]+)\\"").matcher(raw);
                        while (mWiki.find() && wikiUrls.size() < 6) wikiUrls.add(mWiki.group(1));
                    }
                }
            } catch (Exception e) {}

            StringBuilder sb = new StringBuilder();
            sb.append("<html><body style='padding: 30px; font-family: \\"Segoe UI\\", sans-serif; background-color: #121212; color: #e0e0e0;'>");
            sb.append("<div style='background-color: #2563eb; padding: 25px; border-radius: 12px; margin-bottom: 30px;'>");
            sb.append("<h1 style='margin: 0; color: #ffffff; font-size: 28px;'>🔍 Căutare Globală: ").append(escapeHtml(query)).append("</h1>");
            sb.append("<p style='margin: 8px 0 0 0; color: #e2e8f0; font-size: 14px;'>Rezultate unificate</p>");
            sb.append("</div>");
            
            if (!wikiUrls.isEmpty()) {
                sb.append("<h2 style='color: #a78bfa; border-bottom: 2px solid #3f3f46; padding-bottom: 8px; margin-bottom: 20px;'>📚 Articole Relevante</h2>");
                for (String wUrl : wikiUrls) {
                    sb.append("<div style='background-color: #1e1e1e; padding: 15px; border-radius: 8px; margin-bottom: 15px;'>");
                    sb.append("<a href='").append(escapeHtml(wUrl)).append("' style='color: #38bdf8; text-decoration: none; font-size: 18px; font-weight: bold;'>").append(escapeHtml(wUrl)).append("</a>");
                    sb.append("</div>");
                }
            }
            
            sb.append("<hr style='border: 0; border-top: 1px solid #3f3f46; margin: 30px 0;'>");
            sb.append("<p style='font-size: 14px; text-align: center; color: #a1a1aa;'>Căutare procesată intern de Nexus Browser</p>");
            sb.append("</body></html>");
            return new PageLoadResult(urlToLoad, "Căutare: " + query, sb.toString(), false);
        }"""

content = re.sub(r'private PageLoadResult buildGlobalSearchResults\(String query\) \{.*?(?=private String unescapeJson\(String text\))', new_search + '\n\n        ', content, flags=re.DOTALL)

with open('lab4/src/BrowserTab.java', 'w') as f:
    f.write(content)
