package com.beijunyi.parallelgit.web.connection;

import javax.annotation.Nonnull;
import javax.websocket.*;

import com.beijunyi.parallelgit.web.config.GsonFactory;
import com.beijunyi.parallelgit.web.protocol.ClientRequest;
import com.google.gson.Gson;

public class JsonDecoder implements Decoder.Text<ClientRequest> {

  private final Gson gson = GsonFactory.getInstance();

  @Override
  public ClientRequest decode(@Nonnull String str) throws DecodeException {
    return gson.fromJson(str, ClientRequest.class);
  }

  @Override
  public void init(@Nonnull EndpointConfig config) {
  }

  @Override
  public boolean willDecode(String s) {
    return true;
  }

  @Override
  public void destroy() {
  }

}
