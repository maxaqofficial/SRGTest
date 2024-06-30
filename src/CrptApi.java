import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CrptApi {
    UsageController usageController;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.usageController = new UsageController(timeUnit, requestLimit);
    }

    public void createDocument(Document document, String sig) throws IOException, InterruptedException {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(document);
        usageController.prepareToSend();

        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("https://ismp.crpt.ru/api/v3/lk/documents/create");
        StringEntity entity = new StringEntity(json);
        httpPost.setEntity(entity);
        httpPost.setHeader("Content-Type","application/json");
        httpPost.setHeader("Signature", sig);
        CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(httpPost);
        HttpEntity responseEntity = response.getEntity();
        String responseString = EntityUtils.toString(responseEntity, "UTF-8");

        usageController.incrementAndGet();
        System.out.println("Ответ от сервера: " + responseString);
    }
}

class UsageController {
    private TimeUnit timeUnit;
    private int requestLimit;
    private AtomicInteger counter;
    private Date lastCall;

    public UsageController(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit = timeUnit;
        this.requestLimit = requestLimit;
        counter = new AtomicInteger(0);
        lastCall = new Date();
    }

    public synchronized void incrementAndGet() {
        counter.incrementAndGet();
    }
    public synchronized void prepareToSend() throws InterruptedException {
        long currentTime = System.currentTimeMillis();
        long passedTime = currentTime - lastCall.getTime();
        if(passedTime >= timeUnit.toMillis(1)) {
            counter.set(0);
            lastCall = new Date(currentTime);
        }
        while(counter.get() >= requestLimit) {
            wait(timeUnit.toMillis(1) - passedTime);
            currentTime = System.currentTimeMillis();
            passedTime = currentTime - lastCall.getTime();
            if(passedTime >= timeUnit.toMillis(1)) {
                counter.set(0);
                lastCall = new Date(currentTime);
            }
        }
    }
}

class Document {
    Description description;
    String doc_id;
    String doc_status;
    String doc_type;
    boolean importRequest;
    String owner_inn;
    String participant_inn;
    String producer_inn;
    String production_date;
    String production_type;
    List<Product> products;
    String reg_date;
    String reg_number;


    public Document(Description description, String doc_id, String doc_status, String doc_type,
                    boolean importRequest, String owner_inn, String participant_inn,
                    String producer_inn, String production_date, String production_type,
                    List<Product> products, String reg_date, String reg_number) {
        this.description = description;
        this.doc_id = doc_id;
        this.doc_status = doc_status;
        this.doc_type = doc_type;
        this.importRequest = importRequest;
        this.owner_inn = owner_inn;
        this.participant_inn = participant_inn;
        this.producer_inn = producer_inn;
        this.production_date = production_date;
        this.production_type = production_type;
        this.products = products;
        this.reg_date = reg_date;
        this.reg_number = reg_number;
    }

    public Description getDescription() {
        return description;
    }

    public void setDescription(Description description) {
        this.description = description;
    }

    public String getDoc_id() {
        return doc_id;
    }

    public void setDoc_id(String doc_id) {
        this.doc_id = doc_id;
    }

    public String getDoc_status() {
        return doc_status;
    }

    public void setDoc_status(String doc_status) {
        this.doc_status = doc_status;
    }

    public String getDoc_type() {
        return doc_type;
    }

    public void setDoc_type(String doc_type) {
        this.doc_type = doc_type;
    }

    public boolean isImportRequest() {
        return importRequest;
    }

    public void setImportRequest(boolean importRequest) {
        this.importRequest = importRequest;
    }

    public String getOwner_inn() {
        return owner_inn;
    }

    public void setOwner_inn(String owner_inn) {
        this.owner_inn = owner_inn;
    }

    public String getParticipant_inn() {
        return participant_inn;
    }

    public void setParticipant_inn(String participant_inn) {
        this.participant_inn = participant_inn;
    }

    public String getProducer_inn() {
        return producer_inn;
    }

    public void setProducer_inn(String producer_inn) {
        this.producer_inn = producer_inn;
    }

    public String getProduction_date() {
        return production_date;
    }

    public void setProduction_date(String production_date) {
        this.production_date = production_date;
    }

    public String getProduction_type() {
        return production_type;
    }

    public void setProduction_type(String production_type) {
        this.production_type = production_type;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public String getReg_date() {
        return reg_date;
    }

    public void setReg_date(String reg_date) {
        this.reg_date = reg_date;
    }

    public String getReg_number() {
        return reg_number;
    }

    public void setReg_number(String reg_number) {
        this.reg_number = reg_number;
    }
}

class Description {
    String prticipantInn;

    public Description(String prticipantInn) {
        this.prticipantInn = prticipantInn;
    }

    public String getPrticipantInn() {
        return prticipantInn;
    }

    public void setPrticipantInn(String prticipantInn) {
        this.prticipantInn = prticipantInn;
    }
}

class Product {
    String certificate_document;
    String certificate_document_date;
    String certificate_document_number;
    String owner_inn;
    String tnved_code;
    String uit_code;
    String uitu_code;

    public Product(String certificate_document, String certificate_document_date,
                   String certificate_document_number, String owner_inn,
                   String tnved_code, String uit_code, String uitu_code) {
        this.certificate_document = certificate_document;
        this.certificate_document_date = certificate_document_date;
        this.certificate_document_number = certificate_document_number;
        this.owner_inn = owner_inn;
        this.tnved_code = tnved_code;
        this.uit_code = uit_code;
        this.uitu_code = uitu_code;
    }

    public String getCertificate_document() {
        return certificate_document;
    }

    public void setCertificate_document(String certificate_document) {
        this.certificate_document = certificate_document;
    }

    public String getCertificate_document_date() {
        return certificate_document_date;
    }

    public void setCertificate_document_date(String certificate_document_date) {
        this.certificate_document_date = certificate_document_date;
    }

    public String getCertificate_document_number() {
        return certificate_document_number;
    }

    public void setCertificate_document_number(String certificate_document_number) {
        this.certificate_document_number = certificate_document_number;
    }

    public String getOwner_inn() {
        return owner_inn;
    }

    public void setOwner_inn(String owner_inn) {
        this.owner_inn = owner_inn;
    }

    public String getTnved_code() {
        return tnved_code;
    }

    public void setTnved_code(String tnved_code) {
        this.tnved_code = tnved_code;
    }

    public String getUit_code() {
        return uit_code;
    }

    public void setUit_code(String uit_code) {
        this.uit_code = uit_code;
    }

    public String getUitu_code() {
        return uitu_code;
    }

    public void setUitu_code(String uitu_code) {
        this.uitu_code = uitu_code;
    }
}


