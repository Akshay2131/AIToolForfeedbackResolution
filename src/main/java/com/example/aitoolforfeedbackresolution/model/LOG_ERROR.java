package com.example.aitoolforfeedbackresolution.model;

import com.example.aitoolforfeedbackresolution.InOut;
import jakarta.persistence.*;

import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "LOG_ERROR")
public class LOG_ERROR {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SNO")
    private Long sno;

    @Column(name = "XML_GUID")
    private String xmlGuid;

    @Enumerated(EnumType.STRING)
    @Column(name = "IN_OUT")
    private InOut inOut;

    @Column(name = "DATE_TIME")
    private Date dateTime;

    @Column(name = "XML")
    private String xml;

    @Column(name = "RESPONSE_XML")
    private String responseXml;

    @Column(name = "ERROR_MESSAGE")
    private String errorMessage;

    @Column(name = "FAIL_REASN")
    private String failReason;

    @Column(name = "MAIN_ID_XML")
    @Transient
    private String mainIdXml;

    @Column(name = "RESPONSE_CODE")
    private Integer responseCode;

    public Long getSno() {
        return sno;
    }

    public void setSno(Long sno) {
        this.sno = sno;
    }

    public String getXmlGuid() {
        return xmlGuid;
    }

    public void setXmlGuid(String xmlGuid) {
        this.xmlGuid = xmlGuid;
    }

    public InOut getInOut() {
        return inOut;
    }

    public void setInOut(InOut inOut) {
        this.inOut = inOut;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public String getResponseXml() {
        return responseXml;
    }

    public void setResponseXml(String responseXml) {
        this.responseXml = responseXml;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getFailReason() {
        return failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }

    public String getMainIdXml() {
        return mainIdXml;
    }

    public void setMainIdXml(String mainIdXml) {
        this.mainIdXml = mainIdXml;
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }

    @PrePersist
    public void prePersist() {
        if (Objects.isNull(dateTime))
            dateTime = new Date();
    }

}
