package com.fcs.vertx.server;

import com.fcs.vertx.entidade.Contato;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MainVerticle extends AbstractVerticle {

    private Map<Integer, Contato> contatos = new TreeMap<>();
    private static final AtomicInteger COUNTER = new AtomicInteger();

    @Override
    public void start(Future<Void> future) {

        criarContatoMocks();

        Router router = Router.router(vertx);

        router.route("/").handler(StaticHandler.create("www"));
        router.route("/api/contatos*").handler(BodyHandler.create());
        router.post("/api/contatos").handler(this::criarContato);
        router.get("/api/contatos").handler(this::pegarTodos);
        router.get("/api/contatos/:id").handler(this::procurarPorId);
        router.put("/api/contatos/:id").handler(this::atualizar);
        router.delete("/api/contatos/:id").handler(this::apagar);

        vertx.createHttpServer().requestHandler(router::accept).listen(config().getInteger("http.port", 9000),
        result -> {
            if (result.succeeded()) {
                future.complete();
            } else {
                future.fail(result.cause());
            }
        });

    }

    private void criarContato(RoutingContext routingContext) {
        final Contato contato = Json.decodeValue(routingContext.getBodyAsString(), Contato.class);
        contato.setId(COUNTER.getAndIncrement());
        contatos.put(contato.getId(), contato);

        routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8").end(Json.encodePrettily(contato));
    }

    private void pegarTodos(RoutingContext routingContext) {
        routingContext.response().putHeader("content-type", "application/json; charset=utf-8").end(Json.encodePrettily(contatos.values()));
    }

    private void procurarPorId(RoutingContext routingContext) {
        final String id = routingContext.request().getParam("id");
        if (id == null) {
            routingContext.response().setStatusCode(400).end();
        } else {
            final Integer idAsInteger = Integer.valueOf(id);
            Contato contato = contatos.get(idAsInteger);
            if (contato == null) {
                routingContext.response().setStatusCode(404).end();
            } else {
                routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
                        .end(Json.encodePrettily(contato));
            }
        }
    }

    private void atualizar(RoutingContext routingContext) {
        final String id = routingContext.request().getParam("id");
        JsonObject json = routingContext.getBodyAsJson();
        if (id == null || json == null) {
            routingContext.response().setStatusCode(400).end();
        } else {
            final Integer idAsInteger = Integer.valueOf(id);
            Contato contato = contatos.get(idAsInteger);
            if (contato == null) {
                routingContext.response().setStatusCode(404).end();
            } else {
                contato.setNome(json.getString("nome"));
                contato.setTelefone(json.getString("telefone"));
                routingContext.response().putHeader("content-type", "application/json; charset=utf-8").end(Json.encodePrettily(contato));
            }
        }
    }

    private void apagar(RoutingContext routingContext) {
        String id = routingContext.request().getParam("id");
        if (id == null) {
            routingContext.response().setStatusCode(400).end();
        } else {
            Integer idAsInteger = Integer.valueOf(id);
            contatos.remove(idAsInteger);
        }
        routingContext.response().setStatusCode(204).end();
    }

    private void criarContatoMocks() {
        for (int i = 0; i < 10; i++) {
            Contato contato = new Contato();
            contato.setId(COUNTER.getAndIncrement());
            contato.setNome("Fernando");
            contato.setTelefone("(48) 3222-0000");
            contatos.put(contato.getId(), contato);
        }

    }
    
}
