package milor_backend.service;

import org.springframework.context.ApplicationEvent;

public class VentaRegistradaEvent extends ApplicationEvent {
    public VentaRegistradaEvent(Object source) {
        super(source);
    }
}