package com.book.keeping.service;

import com.book.keeping.model.AiInsight;
import com.book.keeping.model.ChatMessage;
import com.book.keeping.model.ChatResponse;
import com.book.keeping.model.ChatSession;
import com.book.keeping.model.Transaction;
import com.book.keeping.util.ApiConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DeepSeek AI服务类
 * 处理与DeepSeek API的通信，生成财务洞察和聊天功能
 */
public class DeepSeekService {
    // 系统提示模板 - 财务顾问角色
    private static final String SYSTEM_PROMPT_TEMPLATE = 
            "你是一位专业的个人财务顾问，精通理财、预算规划和投资策略。" +
            "基于用户的交易历史和财务状况，提供专业、实用的建议。" +
            "确保你的回答友好、专业且具体。" +
            "你必须分析并引用用户的实际交易数据，不要提供通用建议。" +
            "如果用户询问特定财务领域，请使用他们的交易历史来提供个性化分析。" +
            "用户的财务概况：%s\n\n" +
            "在每次回复中，都要引用用户的实际交易数据来支持你的分析和建议。";
    
    private final ObjectMapper objectMapper;
    
    public DeepSeekService() {
        this.objectMapper = new ObjectMapper();
        // 确保API配置已初始化
        ApiConfig.init();
    }
    
    /**
     * 基于交易数据生成AI洞察
     * @param transactions 交易数据列表
     * @return AI洞察对象
     */
    public AiInsight generateInsight(List<Transaction> transactions) throws IOException {
        String prompt = createPrompt(transactions);
        String aiResponse = callDeepSeekApi(prompt);
        return parseAiResponse(aiResponse);
    }
    
    /**
     * 创建发送给DeepSeek的提示文本
     * @param transactions 交易数据列表
     * @return 格式化的提示文本
     */
    private String createPrompt(List<Transaction> transactions) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("基于以下交易数据，请分析用户的消费模式，并提供以下洞察：\n\n");
        promptBuilder.append("1. 建议的月度预算金额\n");
        promptBuilder.append("2. 建议的月度储蓄目标金额\n");
        promptBuilder.append("3. 至少3条成本削减建议\n");
        promptBuilder.append("4. 财务状况概览\n\n");
        promptBuilder.append("请确保生成的回复采用以下JSON格式：\n");
        promptBuilder.append("{\n");
        promptBuilder.append("  \"monthlyBudget\": 数值,\n");
        promptBuilder.append("  \"savingsGoal\": 数值,\n");
        promptBuilder.append("  \"costReductionSuggestions\": [\"建议1\", \"建议2\", \"建议3\"],\n");
        promptBuilder.append("  \"overview\": \"财务状况概览文字\"\n");
        promptBuilder.append("}\n\n");
        promptBuilder.append("交易数据（格式：日期,描述,金额,类别,来源）：\n");
        
        // 添加最近90天的交易数据
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        transactions.stream()
                .limit(90) // 限制为最近的90条记录
                .forEach(t -> promptBuilder.append(
                        t.getDate().format(formatter)).append(",")
                        .append(t.getDescription()).append(",")
                        .append(t.getAmount()).append(",")
                        .append(t.getCategory()).append(",")
                        .append(t.getSource()).append("\n")
                );
        
        return promptBuilder.toString();
    }
    
    /**
     * 调用DeepSeek API
     * @param prompt 提示文本
     * @return API响应
     */
    private String callDeepSeekApi(String prompt) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(ApiConfig.getDeepseekApiUrl());
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Authorization", "Bearer " + ApiConfig.getDeepseekApiKey());
            
            // 创建请求体
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", ApiConfig.getDeepseekModel());
            
            ArrayNode messagesArray = requestBody.putArray("messages");
            ObjectNode userMessage = messagesArray.addObject();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 1000);
            
            String jsonPayload = objectMapper.writeValueAsString(requestBody);
            request.setEntity(new StringEntity(jsonPayload));
            
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String result = EntityUtils.toString(entity);
                    JsonNode responseNode = objectMapper.readTree(result);
                    
                    // 检查是否有错误
                    if (responseNode.has("error")) {
                        JsonNode errorNode = responseNode.get("error");
                        String errorMessage = errorNode.has("message") 
                            ? errorNode.get("message").asText() 
                            : "未知错误";
                        throw new IOException("API错误: " + errorMessage);
                    }
                    
                    JsonNode choicesNode = responseNode.get("choices");
                    if (choicesNode != null && choicesNode.isArray() && choicesNode.size() > 0) {
                        JsonNode firstChoice = choicesNode.get(0);
                        JsonNode messageNode = firstChoice.get("message");
                        if (messageNode != null) {
                            JsonNode contentNode = messageNode.get("content");
                            if (contentNode != null) {
                                return contentNode.asText();
                            }
                        }
                    }
                }
                throw new IOException("The content cannot be obtained from the API response");
            }
        }
    }
    
    /**
     * 解析AI响应，提取结构化的洞察数据
     * @param aiResponse API响应文本
     * @return 结构化的AI洞察对象
     */
    private AiInsight parseAiResponse(String aiResponse) throws IOException {
        // 提取JSON部分
        int jsonStartIndex = aiResponse.indexOf('{');
        int jsonEndIndex = aiResponse.lastIndexOf('}') + 1;
        
        if (jsonStartIndex == -1 || jsonEndIndex == 0 || jsonEndIndex <= jsonStartIndex) {
            throw new IOException("There is no valid JSON data in the AI response");
        }
        
        String jsonPart = aiResponse.substring(jsonStartIndex, jsonEndIndex);
        
        try {
            JsonNode rootNode = objectMapper.readTree(jsonPart);
            
            double monthlyBudget = rootNode.has("monthlyBudget") 
                    ? rootNode.get("monthlyBudget").asDouble() : 0.0;
            
            double savingsGoal = rootNode.has("savingsGoal") 
                    ? rootNode.get("savingsGoal").asDouble() : 0.0;
            
            List<String> suggestions = new ArrayList<>();
            if (rootNode.has("costReductionSuggestions") && rootNode.get("costReductionSuggestions").isArray()) {
                JsonNode suggestionsNode = rootNode.get("costReductionSuggestions");
                for (JsonNode suggestion : suggestionsNode) {
                    suggestions.add(suggestion.asText());
                }
            }
            
            String overview = rootNode.has("overview") 
                    ? rootNode.get("overview").asText() : "";
            
            return new AiInsight(monthlyBudget, savingsGoal, suggestions, overview);
        } catch (Exception e) {
            throw new IOException("An error occurred when parsing the AI response: " + e.getMessage(), e);
        }
    }
    
    /**
     * 模拟DeepSeek API响应（用于开发/测试）
     * @param transactions 交易数据
     * @return 模拟的AI洞察
     */
    public AiInsight generateMockInsight(List<Transaction> transactions) {
        // 计算总收入和总支出
        double totalIncome = transactions.stream()
                .filter(t -> t.getAmount() > 0)
                .mapToDouble(Transaction::getAmount)
                .sum();
        
        double totalExpense = Math.abs(transactions.stream()
                .filter(t -> t.getAmount() < 0)
                .mapToDouble(Transaction::getAmount)
                .sum());
        
        // 计算类别支出
        Map<String, Double> categoryExpenses = transactions.stream()
                .filter(t -> t.getAmount() < 0)
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.summingDouble(t -> Math.abs(t.getAmount()))
                ));
        
        // 找出支出最多的三个类别
        List<Map.Entry<String, Double>> topExpenses = categoryExpenses.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(3)
                .collect(Collectors.toList());
        
        // 生成建议
        List<String> suggestions = new ArrayList<>();
        if (topExpenses.isEmpty()) {
            // 添加默认建议
            suggestions.add("Consider recording more transaction data to obtain more personalized suggestion.");
            suggestions.add("Try to cut down on unnecessary daily expenses, such as takeout and coffee.");
            suggestions.add("Consider formulating a monthly budget plan and rationally allocating various expenditures.");
        } else {
            // 使用现有数据生成建议
            for (Map.Entry<String, Double> entry : topExpenses) {
                suggestions.add(String.format("Consider reducing the expenditure of the %s category, which is currently ¥%.2f",
                        entry.getKey(), entry.getValue()));
            }
            
            // 如果建议数量少于3条，添加一些通用建议
            if (suggestions.size() < 3) {
                if (suggestions.size() < 1) 
                    suggestions.add("Make a detailed monthly budget plan to avoid impulse consumption.");
                if (suggestions.size() < 2) 
                    suggestions.add("Compare the prices of different merchants and look for the most cost-effective option.");
                if (suggestions.size() < 3) 
                    suggestions.add("Consider using automatic savings tools to deposit a fixed portion of your income.");
            }
        }
        
        // 计算建议的预算和储蓄目标
        double avgIncome = totalIncome / Math.max(1, transactions.size() / 30.0); // 每月平均收入
        double avgExpense = totalExpense / Math.max(1, transactions.size() / 30.0); // 每月平均支出
        
        double suggestedBudget = avgExpense * 0.9; // 建议预算为平均支出的90%
        double suggestedSavings = avgIncome * 0.2; // 建议储蓄为平均收入的20%
        
        // 生成概览
        String overview = String.format(
                "Based on your past transaction records, your average monthly income is approximately ¥%.2f，The average monthly expenditure is approximately ¥%.2f " +
                "It is suggested that the monthly budget be controlled at ¥%.2f，monthlySavings ¥%.2f",
                avgIncome, avgExpense, suggestedBudget, suggestedSavings);
        
        return new AiInsight(suggestedBudget, suggestedSavings, suggestions, overview);
    }
    
    /**
     * 基于交易数据和聊天历史进行对话
     * @param chatSession 聊天会话
     * @param userMessage 用户消息
     * @param transactions 交易数据
     * @return 包含用户消息和AI响应的ChatResponse对象
     */
    public ChatResponse chat(ChatSession chatSession, String userMessage, List<Transaction> transactions) throws IOException {
        try {
            // 检查线程是否被中断
            if (Thread.currentThread().isInterrupted()) {
                return new ChatResponse(userMessage, "The request has been cancelled.", true);
            }
            
            // 构建系统提示，注入交易数据上下文
            String financialContext = generateFinancialContext(transactions);
            String systemPrompt = String.format(SYSTEM_PROMPT_TEMPLATE, financialContext);
            
            // 构建API请求
            String aiResponse = callChatApi(systemPrompt, chatSession, userMessage);
            
            // 返回聊天响应，不直接修改会话
            return new ChatResponse(userMessage, aiResponse);
        } catch (IOException e) {
            // 检查是否是取消操作导致的异常
            if (Thread.currentThread().isInterrupted() || e.getMessage().contains("The operation has been cancelled.")) {
                return new ChatResponse(userMessage, "The request has been cancelled.", true);
            }
            
            // 记录错误，但不在UI中显示技术细节
            System.err.println("An error occurred when calling the chat API: " + e.getMessage());
            
            // 返回一个友好的错误消息给用户
            String errorMessage = "Sorry, I'm unable to handle your request for the time being. Please check your network connection and API configuration. Try again later.";
            return new ChatResponse(userMessage, errorMessage, true);
        } catch (Exception e) {
            // 处理其他类型的异常
            if (Thread.currentThread().isInterrupted()) {
                return new ChatResponse(userMessage, "The request has been cancelled.", true);
            }
            
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
            
            String errorMessage = "Sorry, an unexpected error occurred while processing your request.";
            return new ChatResponse(userMessage, errorMessage, true);
        }
    }
    
    /**
     * 模拟聊天响应（用于开发/测试）
     * @param session 聊天会话
     * @param userMessage 用户消息
     * @param transactions 交易数据
     * @return 包含用户消息和AI响应的ChatResponse对象
     */
    public ChatResponse mockChat(ChatSession session, String userMessage, List<Transaction> transactions) {
        // 生成简单的财务建议回复
        String aiResponse;
        String userMessageLower = userMessage.toLowerCase();
        
        // 分析用户消息内容，生成相应的回复
        if (userMessageLower.contains("预算") || userMessageLower.contains("budget")) {
            double avgExpense = calculateAverageMonthlyExpense(transactions);
            aiResponse = String.format(
                    "基于您的交易记录，我建议您的月度预算控制在 ¥%.2f 左右。建议将预算分配到不同的支出类别，并定期跟踪您的支出情况。",
                    avgExpense * 0.9);
        } else if (userMessageLower.contains("储蓄") || userMessageLower.contains("saving")) {
            double avgIncome = calculateAverageMonthlyIncome(transactions);
            aiResponse = String.format(
                    "理财专家通常建议将收入的20%%用于储蓄。根据您的收入情况，建议每月储蓄 ¥%.2f。您可以考虑设立紧急备用金、退休储蓄和短期储蓄目标。",
                    avgIncome * 0.2);
        } else if (userMessageLower.contains("投资") || userMessageLower.contains("invest")) {
            aiResponse = "投资前，建议先确保您有足够的紧急备用金（通常为3-6个月的生活开支）。投资组合可以考虑股票、债券和基金的多元化配置，根据您的风险承受能力调整各类资产的比例。";
        } else if (userMessageLower.contains("消费") || userMessageLower.contains("支出") || userMessageLower.contains("expense")) {
            // 找出主要支出类别
            Map<String, Double> topExpenses = calculateTopExpenses(transactions, 3);
            StringBuilder expenseAnalysis = new StringBuilder("您的主要支出类别是：");
            
            if (topExpenses.isEmpty()) {
                aiResponse = "您的交易记录中暂无足够的支出数据进行分析。建议您记录更多的日常支出，我才能提供更精准的消费分析。";
            } else {
                for (Map.Entry<String, Double> entry : topExpenses.entrySet()) {
                    expenseAnalysis.append(String.format("\n- %s: ¥%.2f", entry.getKey(), entry.getValue()));
                }
                expenseAnalysis.append("\n\n建议关注这些主要支出，寻找可能的节约空间。");
                aiResponse = expenseAnalysis.toString();
            }
        } else {
            // 默认回复
            aiResponse = "作为您的财务顾问，我可以帮助您分析预算、储蓄策略、支出模式和投资建议。请告诉我您想了解哪方面的财务信息？";
        }
        
        // 返回聊天响应，不直接修改会话
        return new ChatResponse(userMessage, aiResponse);
    }
    
    /**
     * 调用DeepSeek聊天API
     * @param systemPrompt 系统提示
     * @param session 聊天会话
     * @param userMessage 当前用户消息
     * @return API响应
     */
    private String callChatApi(String systemPrompt, ChatSession session, String userMessage) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // 检查线程是否被中断
            if (Thread.currentThread().isInterrupted()) {
                throw new IOException("The operation has been cancelled.");
            }
            
            HttpPost request = new HttpPost(ApiConfig.getDeepseekApiUrl());
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Authorization", "Bearer " + ApiConfig.getDeepseekApiKey());
            
            // 创建请求体
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", ApiConfig.getDeepseekModel());
            
            // 添加消息
            ArrayNode messagesArray = requestBody.putArray("messages");
            
            // 添加系统提示
            ObjectNode systemMessage = messagesArray.addObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);
            
            // 添加聊天历史
            for (ChatMessage message : session.getMessages()) {
                // 再次检查线程是否被中断
                if (Thread.currentThread().isInterrupted()) {
                    throw new IOException("The operation has been cancelled.");
                }
                
                ObjectNode chatMessage = messagesArray.addObject();
                chatMessage.put("role", message.isUserMessage() ? "user" : "assistant");
                chatMessage.put("content", message.getContent());
            }
            
            // 添加当前用户消息
            ObjectNode currentUserMessage = messagesArray.addObject();
            currentUserMessage.put("role", "user");
            currentUserMessage.put("content", userMessage);
            
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 1000);
            
            String jsonPayload = objectMapper.writeValueAsString(requestBody);
            request.setEntity(new StringEntity(jsonPayload, "UTF-8"));
            
            // 调试日志：打印请求参数
            System.out.println("======= DeepSeek API请求 =======");
            System.out.println("API URL: " + ApiConfig.getDeepseekApiUrl());
            System.out.println("系统提示: " + systemPrompt);
            System.out.println("消息历史数量: " + session.getMessages().size());
            System.out.println("当前用户消息: " + userMessage);
            System.out.println("请求体: " + jsonPayload);
            
            // 再次检查线程是否被中断
            if (Thread.currentThread().isInterrupted()) {
                throw new IOException("The operation has been cancelled.");
            }
            
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                // 检查线程是否被中断
                if (Thread.currentThread().isInterrupted()) {
                    throw new IOException("The operation has been cancelled.");
                }
                
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String result = EntityUtils.toString(entity);
                    
                    // 检查线程是否被中断
                    if (Thread.currentThread().isInterrupted()) {
                        throw new IOException("The operation has been cancelled.");
                    }
                    
                    // 调试日志：打印响应
                    System.out.println("======= DeepSeek API响应 =======");
                    System.out.println("状态码: " + response.getStatusLine().getStatusCode());
                    System.out.println("响应体: " + result);
                    
                    JsonNode responseNode = objectMapper.readTree(result);
                    
                    // 检查是否有错误
                    if (responseNode.has("error")) {
                        JsonNode errorNode = responseNode.get("error");
                        String errorMessage = errorNode.has("message") 
                            ? errorNode.get("message").asText() 
                            : "Unknown error";
                        throw new IOException("API error: " + errorMessage);
                    }
                    
                    JsonNode choicesNode = responseNode.get("choices");
                    if (choicesNode != null && choicesNode.isArray() && choicesNode.size() > 0) {
                        JsonNode firstChoice = choicesNode.get(0);
                        JsonNode messageNode = firstChoice.get("message");
                        if (messageNode != null) {
                            JsonNode contentNode = messageNode.get("content");
                            if (contentNode != null) {
                                return contentNode.asText();
                            }
                        }
                    }
                }
                throw new IOException("The content cannot be obtained from the API response");
            }
        } catch (IOException e) {
            // 检查是否是由于中断导致的异常
            if (Thread.currentThread().isInterrupted()) {
                throw new IOException("The operation has been cancelled.");
            }
            throw e;
        }
    }
    
    /**
     * 生成财务上下文摘要，用于注入到系统提示中
     * @param transactions 交易数据
     * @return 财务上下文摘要
     */
    private String generateFinancialContext(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return "The user has no transaction data for the moment";
        }
        
        // 计算总收入和总支出
        double totalIncome = transactions.stream()
                .filter(t -> t.getAmount() > 0)
                .mapToDouble(Transaction::getAmount)
                .sum();
        
        double totalExpense = Math.abs(transactions.stream()
                .filter(t -> t.getAmount() < 0)
                .mapToDouble(Transaction::getAmount)
                .sum());
        
        // 计算月平均收入和支出
        double avgIncome = calculateAverageMonthlyIncome(transactions);
        double avgExpense = calculateAverageMonthlyExpense(transactions);
        
        // 获取主要支出类别
        Map<String, Double> topExpenses = calculateTopExpenses(transactions, 3);
        StringBuilder topExpensesStr = new StringBuilder();
        for (Map.Entry<String, Double> entry : topExpenses.entrySet()) {
            topExpensesStr.append(entry.getKey()).append("（¥").append(String.format("%.2f", entry.getValue())).append("）、");
        }
        if (topExpensesStr.length() > 0) {
            topExpensesStr.setLength(topExpensesStr.length() - 1); // 删除最后一个顿号
        }
        
        // 获取最近的交易记录
        LocalDate mostRecentDate = transactions.stream()
                .map(Transaction::getDate)
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now());
        
        LocalDate oldestDate = transactions.stream()
                .map(Transaction::getDate)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());
        
        // 交易时间跨度（天）
        long daySpan = ChronoUnit.DAYS.between(oldestDate, mostRecentDate) + 1;
        
        // 计算储蓄率
        double savingsRate = avgIncome > 0 ? ((avgIncome - avgExpense) / avgIncome * 100) : 0;
        
        // 最大单笔收入和支出
        double maxIncome = transactions.stream()
                .filter(t -> t.getAmount() > 0)
                .mapToDouble(Transaction::getAmount)
                .max()
                .orElse(0);
        
        double maxExpense = Math.abs(transactions.stream()
                .filter(t -> t.getAmount() < 0)
                .mapToDouble(Transaction::getAmount)
                .min()
                .orElse(0));
        
        // 交易频率（平均每天交易次数）
        double transactionFrequency = daySpan > 0 ? ((double) transactions.size() / daySpan) : 0;
        
        // 构建上下文
        StringBuilder context = new StringBuilder();
        context.append(String.format("数据概览：共%d笔交易，数据跨度%d天，平均每天%.1f笔交易。\n", 
                transactions.size(), daySpan, transactionFrequency));
        context.append(String.format("月平均收入：¥%.2f，", avgIncome));
        context.append(String.format("月平均支出：¥%.2f，", avgExpense));
        context.append(String.format("月度收支差：¥%.2f，", avgIncome - avgExpense));
        context.append(String.format("总收入：¥%.2f，", totalIncome));
        context.append(String.format("总支出：¥%.2f，", totalExpense));
        context.append(String.format("总余额：¥%.2f。\n", totalIncome - totalExpense));
        context.append(String.format("收支比率：%.2f%%，", avgIncome > 0 ? (avgExpense / avgIncome * 100) : 0));
        context.append(String.format("储蓄率：%.2f%%，", savingsRate));
        context.append(String.format("最大单笔收入：¥%.2f，", maxIncome));
        context.append(String.format("最大单笔支出：¥%.2f。\n", maxExpense));
        context.append("主要支出类别：").append(topExpensesStr.length() > 0 ? topExpensesStr.toString() : "暂无数据");
        
        return context.toString();
    }
    
    /**
     * 计算月平均收入
     * @param transactions 交易数据
     * @return 月平均收入
     */
    private double calculateAverageMonthlyIncome(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return 0.0;
        }
        
        double totalIncome = transactions.stream()
                .filter(t -> t.getAmount() > 0)
                .mapToDouble(Transaction::getAmount)
                .sum();
        
        return totalIncome / Math.max(1, transactions.size() / 30.0);
    }
    
    /**
     * 计算月平均支出
     * @param transactions 交易数据
     * @return 月平均支出
     */
    private double calculateAverageMonthlyExpense(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return 0.0;
        }
        
        double totalExpense = Math.abs(transactions.stream()
                .filter(t -> t.getAmount() < 0)
                .mapToDouble(Transaction::getAmount)
                .sum());
        
        return totalExpense / Math.max(1, transactions.size() / 30.0);
    }
    
    /**
     * 计算主要支出类别
     * @param transactions 交易数据
     * @param limit 返回数量
     * @return 主要支出类别和金额
     */
    private Map<String, Double> calculateTopExpenses(List<Transaction> transactions, int limit) {
        if (transactions == null || transactions.isEmpty()) {
            return new HashMap<>();
        }
        
        return transactions.stream()
                .filter(t -> t.getAmount() < 0)
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.summingDouble(t -> Math.abs(t.getAmount()))
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        HashMap::new
                ));
    }
} 