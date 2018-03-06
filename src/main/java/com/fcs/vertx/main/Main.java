package com.fcs.vertx.main;

import com.fcs.vertx.server.MainVerticle;
import io.vertx.core.Vertx;

public class Main {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MainVerticle());
        System.out.println(">>> Vertx iniciado e rodando na porta 9000");
    }

}
