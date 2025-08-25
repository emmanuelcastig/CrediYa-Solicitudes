package co.com.pragma.consumer;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ObjectRequest {

    private String documentoIdentidad;

}
