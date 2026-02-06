package com.Saurabh.ETA.Io.Settings;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
@AllArgsConstructor
public class UpdateRequest {

    @NotNull(message = "Name cannot be null")
    String fullName;
    @NotNull(message = "Url cannot be null")
    String profileUrl;

}
