package com.program.file_conversion_service.api.controller;

import com.program.file_conversion_service.api.dto.ConversionTaskResponse;
import com.program.file_conversion_service.api.dto.CreateConversionRequest;
import com.program.file_conversion_service.api.dto.UploadConversionRequest;
import com.program.file_conversion_service.api.mapper.ConversionRequestMapper;
import com.program.file_conversion_service.api.mapper.ConversionTaskResponseMapper;
import com.program.file_conversion_service.domain.model.ConversionTaskEntity;
import com.program.file_conversion_service.service.request.ConversionRequestService;
import com.program.file_conversion_service.service.storage.FileUploadService;
import com.program.file_conversion_service.service.task.ConversionTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

@Tag(name = "Conversions", description = "API for submitting file conversion tasks and retrieving their status")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/conversions")
public class ConversionController {

    private final ConversionRequestService conversionRequestService;
    private final ConversionTaskService conversionTaskService;
    private final FileUploadService fileUploadService;
    private final ConversionRequestMapper conversionRequestMapper;
    private final ConversionTaskResponseMapper conversionTaskResponseMapper;

    @Operation(summary = "Submit a conversion request for an existing object in MinIO")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "202",
                    description = "Conversion task accepted",
                    content = @Content(schema = @Schema(implementation = ConversionTaskResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping(path = "/requests", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConversionTaskResponse> submitRequest(@Valid @RequestBody CreateConversionRequest request) {
        ConversionTaskEntity task = conversionRequestService.submit(conversionRequestMapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(conversionTaskResponseMapper.toResponse(task));
    }

    @Operation(
            summary = "Upload a file and submit it for conversion"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "202",
                    description = "File uploaded and conversion task accepted",
                    content = @Content(schema = @Schema(implementation = ConversionTaskResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid multipart request"),
            @ApiResponse(responseCode = "502", description = "Source file could not be stored"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping(path = "/uploads", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ConversionTaskResponse> uploadAndSubmit(
            @Parameter(
                    description = "Source file to upload and convert",
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
            )
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "Optional metadata for upload and conversion task")
            @ModelAttribute UploadConversionRequest request
    ) throws java.io.IOException {
        ConversionTaskEntity task = fileUploadService.upload(file, request.bucket(), request.fileType(), request.taskId());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(conversionTaskResponseMapper.toResponse(task));
    }

    @Operation(
            summary = "Get conversion task by ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Conversion task found",
                    content = @Content(schema = @Schema(implementation = ConversionTaskResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Conversion task not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/{taskId}")
    public ConversionTaskResponse getById(@PathVariable UUID taskId) {
        return conversionTaskResponseMapper.toResponse(conversionTaskService.getById(taskId));
    }
}
