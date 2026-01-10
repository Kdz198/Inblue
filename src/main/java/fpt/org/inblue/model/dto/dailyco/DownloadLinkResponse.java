package fpt.org.inblue.model.dto.dailyco;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DownloadLinkResponse {
    @JsonProperty("download_link")
    private String downloadLink;
}
