package dev.vuis.plusfront.fetch;

import com.google.gson.Gson;
import dev.vuis.plusfront.server.config.PFServerConfig;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;

public final class BfApi {
	private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
		.connectTimeout(Duration.ofSeconds(10))
		.build();
	private static final Gson GSON = new Gson();

	private BfApi() {
		throw new AssertionError();
	}

	public static @NotNull CompletableFuture<@NotNull Inventory> fetchPlayerInventoryEquipped(@NotNull UUID playerUuid) {
		HttpRequest request = HttpRequest.newBuilder()
			.GET()
			.uri(getUri("/api/v1/player_inventory/equipped?uuid=" + playerUuid))
			.build();

		CompletableFuture<HttpResponse<InputStream>> responseFuture = HTTP_CLIENT.sendAsync(
			request,
			HttpResponse.BodyHandlers.ofInputStream()
		);

		return responseFuture.thenApply(response -> {
			if (response.statusCode() != 200) {
				throw new BfApiException("Player inventory fetch failed (HTTP " + response.statusCode() + ")");
			}

			try (
				InputStream body = response.body();
				Reader reader = new InputStreamReader(body, StandardCharsets.UTF_8)
			) {
				return GSON.fromJson(reader, Inventory.class);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}

	private static @NotNull URI getUri(@NotNull String path) {
		return URI.create(PFServerConfig.getBfApiHost() + path);
	}

	public record Inventory(
		@NotNull List<@NotNull Stack> inventory
	) {
		public record Stack(
			int id,
			double mint
		) {
		}
	}
}
