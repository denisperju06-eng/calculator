import re

with open('lab4/src/BrowserTab.java', 'r') as f:
    content = f.read()

new_search = """private PageLoadResult buildGlobalSearchResults(String query) {
            String encoded = query;
            try { encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.name()); } catch (Exception e) {}
            
            List<String> resultsHtml = new ArrayList<>();
            List<String> wikiUrls = new ArrayList<>();
            
            // 1. Direct Link Guessing (for youtube, google, facebook etc)
            String qLower = query.toLowerCase().trim();
            if (qLower.equals("youtube") || qLower.equals("youtube.com")) {
                resultsHtml.add("<div style='background-color: #262626; border-left: 4px solid #ef4444; padding: 15px; border-radius: 8px; margin-bottom: 15px;'><a href='https://www.youtube.com' style='color: #ef4444; text-decoration: none; font-size: 20px; font-weight: bold;'>▶️ YouTube (Site Oficial)</a><br><span style='color: #9ca3af;'>https://www.youtube.com</span></div>");
            } else if (qLower.equals("google") || qLower.equals("google.com")) {
                resultsHtml.add("<div style='background-color: #262626; border-left: 4px solid #3b82f6; padding: 15px; border-radius: 8px; margin-bottom: 15px;'><a href='https://www.google.com' style='color: #3b82f6; text-decoration: none; font-size: 20px; font-weight: bold;'>🔍 Google (Site Oficial)</a><br><span style='color: #9ca3af;'>https://www.google.com</span></div>");
            } else if (qLower.equals("facebook") || qLower.equals("facebook.com")) {
                resultsHtml.add("<div style='background-color: #262626; border-left: 4px solid #1877F2; padding: 15px; border-radius: 8px; margin-bottom: 15px;'><a href='https://www.facebook.com' style='color: #1877F2; text-decoration: none; font-size: 20px; font-weight: bold;'>📘 Facebook</a><br><span style='color: #9ca3af;'>https://www.facebook.com</span></div>");
            } else if (!qLower.contains(" ")) {
                String guessUrl = qLower.contains(".") ? "https://" + qLower : "https://www." + qLower + ".com";
                resultsHtml.add("<div style='background-color: #1e293b; border-left: 4px solid #10b981; padding: 15px; border-radius: 8px; margin-bottom: 15px;'><a href='" + guessUrl + "' style='color: #10b981; text-decoration: none; font-size: 20px; font-weight: bold;'>🌐 Deschide direct " + escapeHtml(qLower) + "</a><br><span style='color: #9ca3af;'>" + guessUrl + "</span></div>");
            }
            
            // 2. Fallback Wikipedia API
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
            sb.append("<h1 style='margin: 0; color: #ffffff; font-size: 28px;'>🔍 Rezultate pentru: ").append(escapeHtml(query)).append("</h1>");
            sb.append("<p style='margin: 8px 0 0 0; color: #e2e8f0; font-size: 14px;'>Nexus Web Search Engine</p>");
            sb.append("</div>");
            
            if (!resultsHtml.isEmpty()) {
                for (String res : resultsHtml) {
                    sb.append(res);
                }
            }
            
            if (!wikiUrls.isEmpty()) {
                sb.append("<h2 style='color: #a78bfa; border-bottom: 2px solid #3f3f46; padding-bottom: 8px; margin-bottom: 20px; margin-top: 30px;'>📚 Articole Wikipedia</h2>");
                for (String wUrl : wikiUrls) {
                    sb.append("<div style='background-color: #1e1e1e; padding: 15px; border-radius: 8px; margin-bottom: 15px;'>");
                    sb.append("<a href='").append(escapeHtml(wUrl)).append("' style='color: #38bdf8; text-decoration: none; font-size: 16px; font-weight: bold;'>").append(escapeHtml(wUrl)).append("</a>");
                    sb.append("</div>");
                }
            }
            
            sb.append("<hr style='border: 0; border-top: 1px solid #3f3f46; margin: 30px 0;'>");
            sb.append("<p style='font-size: 14px; text-align: center; color: #a1a1aa;'>Realizat pentru Internet Browser POO</p>");
            sb.append("</body></html>");
            return new PageLoadResult(urlToLoad, "Căutare: " + query, sb.toString(), false);
        }"""

content = re.sub(r'private PageLoadResult buildGlobalSearchResults\(String query\) \{.*?(?=private String unescapeJson\(String text\))', new_search + '\n\n        ', content, flags=re.DOTALL)

with open('lab4/src/BrowserTab.java', 'w') as f:
    f.write(content)
