package it.cgmconsulting.mspost.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfDocumentInfo;
import it.cgmconsulting.mspost.payload.response.AvgPosts;
import it.cgmconsulting.mspost.payload.response.ReportAuthorResponse;
import it.cgmconsulting.mspost.payload.response.UserPostResponse;
import it.cgmconsulting.mspost.payload.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class XlsService {

    private final PostService postService;

    public InputStream createReport() throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Creazione del foglio excel
        HSSFWorkbook workbook = new HSSFWorkbook();

        // Creazione Sheet "Author Report"
        createAuthorReport(workbook);

        workbook.write(out);
        workbook.close();
        InputStream in = new ByteArrayInputStream(out.toByteArray());
        return in;
    }

    public void createAuthorReport(HSSFWorkbook workbook){
        // Instanziamo il nuovo foglio di lavoro con la sua Label
        HSSFSheet sheet = workbook.createSheet("Author Report");

        // userId, username, totale post scritti, media totale

        int rowNum = 0, columnNum = 0;
        Row row; Cell cell;

        row = sheet.createRow(rowNum);

        // Creazione intestazioni
        String[] labels = {"Id", "Username", "TOT posts", "Media"};
        for(String s : labels){
            cell = row.createCell(columnNum++, CellType.STRING);
            cell.setCellValue(s);
        }
        // List di tutti i WRITER
        List<UserResponse> writers = postService.getUsersByRole("ROLE_WRITER");
        // Conteggio dei post raggrupppati per autore
//        List<ReportAuthorResponse> list = postService.countPostsByAuthor();
        // Lista delle medie raggruppate per post
        List<AvgPosts> avgPosts = postService.getAvgPosts();

        List<UserPostResponse> list = postService.userPost();
        Map<Long, Set<Long>> userPostsMap = new HashMap<>();

        for (UserResponse u : writers){
            userPostsMap.put(u.getId(),
                    list.stream()
                            .filter(p -> p.getUserId()==u.getId())
                            .map(p -> p.getPostId())
                            .collect(Collectors.toSet()));
        }

        List<ReportAuthorResponse> finalList = new ArrayList<>();

        for(Map.Entry<Long, Set<Long>> map : userPostsMap.entrySet()){

            ReportAuthorResponse x = new ReportAuthorResponse(
                    map.getKey(),
                    map.getValue().size(),
                    writers.stream().filter( w -> w.getId() == map.getKey()).findFirst().map(UserResponse::getUsername).get(),
                    calcAvg(map.getValue(), avgPosts));
            finalList.add(x);
        }

        // Scrittura file
        for (ReportAuthorResponse r : finalList){
            columnNum = 0;
            row = sheet.createRow(++rowNum);
            // Id
            cell = row.createCell(columnNum++, CellType.NUMERIC);
            cell.setCellValue(r.getUserId());
            // Username
            cell = row.createCell(columnNum++, CellType.STRING);
            cell.setCellValue(r.getUsername());
            // Totale post scritti
            cell = row.createCell(columnNum++, CellType.NUMERIC);
            cell.setCellValue(r.getTotPosts());
            // Media
            cell = row.createCell(columnNum++, CellType.NUMERIC);
            cell.setCellValue(r.getTotAVG());
        }

        for (int i =0; i < 4; i++){
            sheet.autoSizeColumn(i);
        }

    }

    private double calcAvg(Set<Long> postIds, List<AvgPosts> getAvgPosts){
        Set<Double> avgs = new HashSet<>();
        for(Long l : postIds) {
            for (AvgPosts a : getAvgPosts) {
                if (a.getPostId() == l) {
                    avgs.add(a.getAvg());
                }
            }
        }
        double avg = 0;
        for (Double d : avgs){
            avg += d;
        }
        if(avgs.size() != 0){
            avg /= avgs.size();
        }
        return avg;
    }

    private void addMetaData(Set<String> categories, String author, PdfDocument pdfDocument){
        PdfDocumentInfo info = pdfDocument.getDocumentInfo();
        info.setKeywords(categories.toString());
        info.setAuthor(author);
    }
}
