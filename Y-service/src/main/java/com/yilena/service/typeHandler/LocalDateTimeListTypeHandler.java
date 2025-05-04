package com.yilena.service.typeHandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import java.time.LocalDateTime;


import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(List.class)
public class LocalDateTimeListTypeHandler extends BaseTypeHandler<List<LocalDateTime>> {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(("yyyy-MM-dd HH:mm:ss.SSS"));

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<LocalDateTime> parameter, JdbcType jdbcType) throws SQLException {
        String joined = parameter.stream()
                .map(dt -> "\"" + dt.format(formatter) + "\"") // 添加引号以匹配MySQL输出
                .collect(Collectors.joining(", ", "[", "]"));
        ps.setString(i, joined);
    }

    @Override
    public List<LocalDateTime> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        return parseJsonArray(json);
    }

    @Override
    public List<LocalDateTime> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        return parseJsonArray(json);
    }

    @Override
    public List<LocalDateTime> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        return parseJsonArray(json);
    }

    private List<LocalDateTime> parseJsonArray(String json) {
        if (json == null || json.trim().isEmpty() || json.equals("[]")) {
            return List.of();
        }

        try {
            // 移除JSON数组的方括号并按逗号分割
            String[] parts = json.substring(1, json.length() - 1).split(",");
            return Arrays.stream(parts)
                    .map(String::trim)
                    .map(str -> str.replaceAll("^\"|\"$", "")) // 移除每个元素的引号
                    .map(str -> {
                        // 标准化时间格式（可选）
                        if (str.length() > 23) { // 处理微秒部分超过6位的情况
                            str = str.substring(0, 23);
                        }
                        return LocalDateTime.parse(str, formatter);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON array: " + json, e);
        }
    }
}