package com.dzaitsev.marshmallow.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@EqualsAndHashCode(of = {"id", "primary"})
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Attachment {
    private Integer id;
    private String url;
    private String thumbnailUrl;
    private String fileName;
    private String extension;
    private String contentType;
    private boolean primary;

    public Attachment copy(){
        return Attachment.builder()
                .id(getId())
                .url(getUrl())
                .thumbnailUrl(getThumbnailUrl())
                .fileName(getFileName())
                .extension(getExtension())
                .contentType(getContentType())
                .primary(isPrimary())
                .build();
    }
}
