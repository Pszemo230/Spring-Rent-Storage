package com.spring.account;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import com.spring.config.WebSecurityConfigurationAware;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

@Slf4j
public class UserAuthenticationIntegrationTest extends WebSecurityConfigurationAware {

  private static String SEC_CONTEXT_ATTR = HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

  @Autowired
  private AccountService accountService;

  private Account createNewAccount() {
    Account account = AccountBuilder.anAccount().setEmail("user").setPassword("demo")
        .setFirstName("TEST").setLastName("Last").build();
    log.debug(String.format("Account before save: %s", account));
    accountService.save(account);
    return account;
  }

  @Test
  public void requiresAuthentication() throws Exception {
    mockMvc.perform(get("/account/current"))
        .andExpect(redirectedUrl("http://localhost/signin"));
  }

  @Test
  public void userAuthenticates() throws Exception {
    Account newAccount = createNewAccount();

    mockMvc.perform(post("/authenticate").param("username", newAccount.getEmail())
        .param("password", "demo"))
        .andExpect(redirectedUrl("/"))
        .andExpect(r -> Assert.assertEquals(
            ((SecurityContext) r.getRequest().getSession().getAttribute(SEC_CONTEXT_ATTR))
                .getAuthentication().getName(), newAccount.getEmail()));

  }

  @Test
  public void userAuthenticationFails() throws Exception {
    final String username = "user";
    mockMvc.perform(post("/authenticate").param("username", username).param("password", "invalid"))
        .andExpect(redirectedUrl("/signin?error=1"))
        .andExpect(
            r -> Assert.assertNull(r.getRequest().getSession().getAttribute(SEC_CONTEXT_ATTR)));
  }

}
