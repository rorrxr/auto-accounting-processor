package com.example.autoaccountingprocessor.common.util;

import com.example.autoaccountingprocessor.domain.rule.CategoryRule;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RuleLoader {
    public static Map<String, Map<String, CategoryRule>> load(MultipartFile rulesFile) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            JsonNode root = mapper.readTree(rulesFile.getInputStream());
            JsonNode companies = root.get("companies");

            Map<String, Map<String, CategoryRule>> ruleMap = new HashMap<>();

            for (JsonNode company : companies) {
                String companyId = company.get("company_id").asText();
                Map<String, CategoryRule> keywordToCategory = new HashMap<>();

                for (JsonNode category : company.get("categories")) {
                    String catId = category.get("category_id").asText();
                    String catName = category.get("category_name").asText();

                    for (JsonNode keyword : category.get("keywords")) {
                        keywordToCategory.put(keyword.asText(), new CategoryRule(catId, catName));
                    }
                }
                ruleMap.put(companyId, keywordToCategory);
            }

            return ruleMap;

        } catch (Exception e) {
            log.error("rules.json 파싱 오류", e);
            throw new RuntimeException("rules.json 파싱에 실패했습니다.");
        }
    }

}
