package si.endava.banksimulator.services;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class ElementNotFoundException extends RuntimeException {

    public ElementNotFoundException(String error) {
        super("Error: " + error);
    }
}
