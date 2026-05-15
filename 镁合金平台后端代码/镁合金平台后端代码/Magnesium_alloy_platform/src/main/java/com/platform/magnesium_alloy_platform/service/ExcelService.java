package com.platform.magnesium_alloy_platform.service;

import com.platform.magnesium_alloy_platform.pojo.Excel;
import com.platform.magnesium_alloy_platform.pojo.Pojo_Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/*
* ExcelService类提供了三个功能：解析Excel文件并将数据转化为Excel对象列表
* 统计Excel对象列表中的有效和无效数据量
* 记录上传的Excel文件信息到数据库
* */
@Service
@Slf4j
public class ExcelService {

    private final List<Excel> objects = new ArrayList<>();

    /**
     * 解析 Excel 文件，支持 .xls 和 .xlsx 格式
     * @param filePath 文件路径
     * @param skipHeader 是否跳过表头
     * @return 解析后的 Excel 数据列表
     * @throws IOException 文件读取失败时抛出异常
     */
    public List<Excel> parseExcel(String filePath, boolean skipHeader) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(filePath);
             Workbook workbook = WorkbookFactory.create(fileInputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            boolean isFirstRow = true; // 标记是否为第一行
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                // 跳过第一行（表头）
                if (isFirstRow && skipHeader) {
                    isFirstRow = false;
                    continue;
                }
                isFirstRow = false;

                Excel object = new Excel();

                // 处理第一列
                Cell cell1 = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                if (cell1.getCellTypeEnum() == CellType.NUMERIC) { // 使用 getCellTypeEnum()
                    object.setAveLength(cell1.getNumericCellValue());
                } else if (cell1.getCellTypeEnum() == CellType.STRING) {
                    try {
                        object.setAveLength(Double.parseDouble(cell1.getStringCellValue()));
                    } catch (NumberFormatException e) {
                        object.setAveLength(0.0); // 如果无法转换为数值，设置为默认值
                    }
                } else {
                    object.setAveLength(0.0); // 对其他类型，设置默认值
                }

                // 处理第二列
                Cell cell2 = row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                if (cell2.getCellTypeEnum() == CellType.NUMERIC) { // 使用 getCellTypeEnum()
                    object.setLisanValue(cell2.getNumericCellValue());
                } else if (cell2.getCellTypeEnum() == CellType.STRING) {
                    try {
                        object.setLisanValue(Double.parseDouble(cell2.getStringCellValue()));
                    } catch (NumberFormatException e) {
                        object.setLisanValue(0.0); // 如果无法转换为数值，设置为默认值
                    }
                } else {
                    object.setLisanValue(0.0); // 对其他类型，设置默认值
                }

                // 添加到对象列表
                objects.add(object);
            }
        } catch (InvalidFormatException e) {
            log.error("文件格式无效: {}", filePath, e);
            throw new IOException("文件格式无效，请上传正确的 Excel 文件", e);
        }
        return objects;
    }

    /**
     * 统计 Excel 数据中的有效和无效记录数
     * @return Pojo_Data 对象，包含统计信息
     */
    public Pojo_Data count() {
        int totalCount = 0;
        int unusefulData = 0;

        Pojo_Data data = new Pojo_Data();
        for (Excel s : objects) {
            if (s.getLisanValue() == 0 || s.getAveLength() == 0) {
                unusefulData++;
            }
            totalCount++;
        }

        int usefulData = totalCount - unusefulData;
        data.setUsefulCount(usefulData);
        data.setUnusefulCount(unusefulData);

        log.info("统计结果: 总数据量={}, 有效数据量={}, 无效数据量={}", totalCount, usefulData, unusefulData);
        return data;
    }
}
