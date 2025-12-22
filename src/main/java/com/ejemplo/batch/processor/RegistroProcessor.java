package com.ejemplo.batch.processor;

import com.ejemplo.batch.model.RegistroCSV;
import java.time.LocalDateTime;
import org.springframework.batch.infrastructure.item.ItemProcessor;

public class RegistroProcessor implements ItemProcessor<RegistroCSV, RegistroCSV> {

    @Override
    public RegistroCSV process(RegistroCSV item) throws Exception {
        // Ejemplo de lógica: transformar el nombre a mayúsculas y agregar fecha de proceso
        final String nombreMayus = item.getNombre().toUpperCase();

        final RegistroCSV registroTransformado = new RegistroCSV();
        registroTransformado.setNombre(nombreMayus);
        registroTransformado.setEdad(item.getEdad());
        registroTransformado.setEmail(item.getEmail());
        registroTransformado.setFechaProceso(LocalDateTime.now()); 
        
        // CRÍTICO: Logging para verificar qué registros se están procesando
        System.out.println("🔄 Procesando registro:");
        System.out.println("   📝 Nombre: " + registroTransformado.getNombre());
        System.out.println("   📧 Email: " + registroTransformado.getEmail());
        System.out.println("   🎂 Edad: " + registroTransformado.getEdad());
        System.out.println("   ⏰ Fecha proceso: " + registroTransformado.getFechaProceso());
        
        // Aquí podrías agregar una validación y devolver null para "filtrar" el registro
        // if (registroTransformado.getEdad() < 18) { return null; }

        return registroTransformado;
    }
}