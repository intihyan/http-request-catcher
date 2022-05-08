package ly.dev.echo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainVerticle extends AbstractVerticle {

  private static long PING_INTERVAL_MS = 15_000;
  Map<String, List<ServerWebSocket>> wsConnections = new HashMap<>();

  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    Router router = Router.router(vertx);
    router.post("/test/*").handler(BodyHandler.create());

    router.get("/ws/*").handler( ctx -> ctx.request().toWebSocket());
    router.get("/*").handler(StaticHandler.create().setWebRoot("webroot"));

    router.route("/echo/:path").handler(ctx -> {
      String requested = ctx.pathParam("path");
      List<ServerWebSocket> conns = wsConnections.get(requested);

      if(conns == null || conns.isEmpty()){
        ctx.end();
        return;
      }
      String header = ctx.request().headers().toString();
      String body = (ctx.getBody() == null )? "" : ctx.getBody().toString();
      JsonObject json = new JsonObject();
      json.put("header", header);
      json.put("body", body);
      for(ServerWebSocket sws: conns){

        sws.writeTextMessage(
          json.toString()
        ).onSuccess( result -> {
          System.out.println("Writing websocket message successfully");
        }).onFailure( result -> {
          System.out.println("Writing websocket message failed" + result.toString());
        });
      }
      ctx.end();
    });

    vertx.createHttpServer().requestHandler(router).webSocketHandler( ws -> {
      System.out.println("new websocket connection detected");
      String path = ws.path();
      path = path.substring(path.lastIndexOf("/") + 1);
      wsConnections.computeIfAbsent(path, k -> new ArrayList<>()).add(ws);

      ws.pongHandler(this::handleWebSocketPong);

      String finalPath = path;
      ws.closeHandler(k -> {
        System.out.println("Removing websocket connection");
        wsConnections.get(finalPath).remove(ws);
      });


      vertx.setPeriodic(PING_INTERVAL_MS, timerId -> {
        ws.writePing(Buffer.buffer("PING"));
      });
    }).listen(8888, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port 8888");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }

  void handleWebSocketPong(Buffer buffer){

  }
}
