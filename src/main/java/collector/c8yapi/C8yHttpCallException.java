package collector.c8yapi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.net.URI;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class C8yHttpCallException extends Exception {

    private String message;
    private URI uri;
    private C8yHttpClient httpClient;
    private Exception e;

}