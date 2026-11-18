package com.jc.appfinanzas;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InicioController {
    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String inicio() {
        return """
                <!doctype html>
                <html lang="es">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>AppFinanzas API</title>
                  <style>
                    body{font:16px/1.55 system-ui,Segoe UI,sans-serif;max-width:760px;margin:8vh auto;padding:0 24px;color:#263548;background:#f4f7fb}
                    main{background:#fff;padding:32px;border-radius:16px;box-shadow:0 8px 28px #17365d18}
                    h1{color:#17365d;margin-top:0} code{background:#edf2f8;padding:3px 6px;border-radius:4px}
                    li{margin:8px 0}.ok{color:#19734b;font-weight:600}
                  </style>
                </head>
                <body><main>
                  <h1>AppFinanzas API</h1>
                  <p class="ok">La aplicación está activa.</p>
                  <p>La API administra cuentas, categorías y transacciones. Para proteger tus datos, las operaciones <code>/api/**</code> requieren un token JWT.</p>
                  <h2>Cómo comenzar</h2>
                  <ol>
                    <li>Registra un usuario con <code>POST /auth/register</code> y un JSON con <code>username</code> y <code>password</code> (mínimo 8 caracteres).</li>
                    <li>Inicia sesión con <code>POST /auth/login</code> para recibir el token.</li>
                    <li>Envía el token como <code>Authorization: Bearer &lt;token&gt;</code> al usar los endpoints financieros.</li>
                  </ol>
                  <h2>Rutas principales</h2>
                  <ul>
                    <li><code>/api/cuentas</code> — cuentas y resumen financiero</li>
                    <li><code>/api/cuentas/categorias</code> — categorías</li>
                    <li><code>/api/transacciones</code> — transacciones y filtros</li>
                  </ul>
                  <p>Usa Postman, Insomnia o una herramienta similar para enviar solicitudes POST, PUT y DELETE.</p>
                </main></body></html>
                """;
    }
}
