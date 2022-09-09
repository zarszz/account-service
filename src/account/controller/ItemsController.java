package account.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class ItemsController {
	private final Map<String, Set<String>> items = new ConcurrentHashMap<>();

	@PostMapping("/items")
	public void addItem(@AuthenticationPrincipal UserDetails details, @RequestParam String item) {
		String username = details.getUsername();

		if (items.containsKey(username)) {
			items.get(username).add(item);
		} else {
			items.put(username, new HashSet<>(Set.of(item)));
		}
	}

	@GetMapping("/items")
	public Set<String> getItems(@AuthenticationPrincipal UserDetails details) {
		return items.getOrDefault(details.getUsername(), Set.of());
	}
}