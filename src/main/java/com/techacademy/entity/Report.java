package com.techacademy.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name="reports")
@SQLRestriction("delete_flg = false")
public class Report {


    public String getFormattedReportDate() {
        /*if (reportedAt != null) {*/
              DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
              return reportDate.format(formatter);
        /*}
          return null;*/
    }


    //ID
    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    //日付
    @Column(nullable = false)
    private LocalDateTime reportDate;

    //タイトル
    @Column(nullable = false, length = 100)
    private String title;

    //内容
    @Column(nullable = false)
    private String content;


   // 削除フラグ(論理削除を行うため)
    @Column(columnDefinition="TINYINT", nullable = false)
    private boolean deleteFlg;

    // 登録日時
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 更新日時
    @Column(nullable = false)
    private LocalDateTime updatedAt;


    //社員番号

    @ManyToOne
    @JoinColumn(name = "employee_code")
    private Employee employeeCode;



}