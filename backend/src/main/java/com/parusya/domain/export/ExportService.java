package com.parusya.domain.export;

import com.parusya.domain.checkin.CheckInRepository;
import com.parusya.domain.event.EventRepository;
import com.parusya.domain.participant.ParticipantRepository;
import com.parusya.infra.security.SecurityUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class ExportService {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter D  = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final EventRepository       eventRepository;
    private final ParticipantRepository participantRepository;
    private final CheckInRepository     checkInRepository;

    public ExportService(EventRepository eventRepository,
                         ParticipantRepository participantRepository,
                         CheckInRepository checkInRepository) {
        this.eventRepository       = eventRepository;
        this.participantRepository = participantRepository;
        this.checkInRepository     = checkInRepository;
    }

    @Transactional(readOnly = true)
    public byte[] generateXlsx() throws IOException {
        UUID groupId = SecurityUtils.getGroupId();

        try (var wb = new XSSFWorkbook()) {
            var headerStyle = buildHeaderStyle(wb);
            var dateStyle   = buildDateStyle(wb);

            writeEventos(wb, headerStyle, dateStyle, groupId);
            writeParticipants(wb, headerStyle, dateStyle, groupId);
            writeCheckIns(wb, headerStyle, dateStyle, groupId);

            var out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    // ─── Aba Eventos ──────────────────────────────────────────────────────────

    private void writeEventos(XSSFWorkbook wb, CellStyle header, CellStyle date, UUID groupId) {
        var sheet = wb.createSheet("Eventos");
        var events = eventRepository.findAllByGroupId(groupId,
                org.springframework.data.domain.Pageable.unpaged()).getContent();

        String[] cols = { "ID", "Nome", "Descrição", "Data de início", "Status", "Tags" };
        writeHeader(sheet, header, cols);

        int row = 1;
        for (var ev : events) {
            var r = sheet.createRow(row++);
            cell(r, 0, ev.getId().toString());
            cell(r, 1, ev.getName());
            cell(r, 2, ev.getDescription() != null ? ev.getDescription() : "");
            cellDate(r, 3, ev.getStartDateTime().format(DT));
            cell(r, 4, ev.isActive() ? "Ativo" : "Inativo");
            cell(r, 5, ev.getTags().stream()
                    .map(t -> t.getName()).collect(java.util.stream.Collectors.joining(", ")));
        }

        autosize(sheet, cols.length);
        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, cols.length - 1));
    }

    // ─── Aba Participants ─────────────────────────────────────────────────────

    private void writeParticipants(XSSFWorkbook wb, CellStyle header, CellStyle date, UUID groupId) {
        var sheet = wb.createSheet("Participants");

        // Busca apenas participants que têm ao menos um check-in no grupo
        var checkIns = checkInRepository.findAllByGroupIdForExport(groupId);
        var participants = checkIns.stream()
                .map(c -> c.getParticipant())
                .distinct()
                .sorted(java.util.Comparator.comparing(p -> p.getFullName()))
                .toList();

        String[] cols = { "ID", "Nome", "E-mail", "Telefone", "Sexo", "Data de nascimento" };
        writeHeader(sheet, header, cols);

        int row = 1;
        for (var p : participants) {
            var r = sheet.createRow(row++);
            cell(r, 0, p.getId().toString());
            cell(r, 1, p.getFullName());
            cell(r, 2, p.getEmail());
            cell(r, 3, p.getPhone());
            cell(r, 4, switch (p.getGender()) {
                case MALE   -> "Masculino";
                case FEMALE -> "Feminino";
                case OTHER  -> "Outro";
            });
            cell(r, 5, p.getBirthDate().format(D));
        }

        autosize(sheet, cols.length);
        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, cols.length - 1));
    }

    // ─── Aba Check-ins ────────────────────────────────────────────────────────

    private void writeCheckIns(XSSFWorkbook wb, CellStyle header, CellStyle date, UUID groupId) {
        var sheet = wb.createSheet("Check-ins");
        var checkIns = checkInRepository.findAllByGroupIdForExport(groupId);

        String[] cols = { "ID", "Participant", "Evento", "Horário", "EventStaff" };
        writeHeader(sheet, header, cols);

        int row = 1;
        for (var c : checkIns) {
            var r = sheet.createRow(row++);
            cell(r, 0, c.getId().toString());
            cell(r, 1, c.getParticipant().getFullName());
            cell(r, 2, c.getEvent().getName());
            cellDate(r, 3, c.getTimestamp().format(DT));
            cell(r, 4, c.getStaff() != null ? c.getStaff().getName() : "");
        }

        autosize(sheet, cols.length);
        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, cols.length - 1));
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void writeHeader(Sheet sheet, CellStyle style, String[] labels) {
        var row = sheet.createRow(0);
        for (int i = 0; i < labels.length; i++) {
            var cell = row.createCell(i);
            cell.setCellValue(labels[i]);
            cell.setCellStyle(style);
        }
    }

    private void cell(Row row, int col, String value) {
        row.createCell(col).setCellValue(value != null ? value : "");
    }

    private void cellDate(Row row, int col, String value) {
        row.createCell(col).setCellValue(value != null ? value : "");
    }

    private void autosize(Sheet sheet, int cols) {
        // autoSizeColumn usa java.awt e falha em ambientes headless (Railway, Docker)
        // Larguras fixas em unidades POI (1 caractere ≈ 256 unidades)
        int[] widths = { 10, 28, 32, 28, 16, 20, 36 };  // cobre até 7 colunas
        for (int i = 0; i < cols; i++) {
            int w = i < widths.length ? widths[i] : 20;
            sheet.setColumnWidth(i, w * 256);
        }
    }

    private CellStyle buildHeaderStyle(XSSFWorkbook wb) {
        var style = wb.createCellStyle();
        var font  = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }

    private CellStyle buildDateStyle(XSSFWorkbook wb) {
        // Estilo reservado para uso futuro com células de data nativa POI
        var style  = wb.createCellStyle();
        var format = wb.createDataFormat();
        style.setDataFormat(format.getFormat("dd/mm/yyyy hh:mm"));
        return style;
    }
}