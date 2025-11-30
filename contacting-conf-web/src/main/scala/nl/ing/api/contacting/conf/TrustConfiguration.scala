package nl.ing.api.contacting.conf

import com.ing.apisdk.merak.autoconfigure.trust.servlet.AccessTokenFilterProperties
import jakarta.servlet.Filter
import jakarta.servlet.http.HttpServletRequest
import nl.ing.api.contacting.conf.business.client.ContactingPermissionsProvider
import nl.ing.api.contacting.conf.modules.CoreModule
import nl.ing.api.contacting.conf.modules.ExecutionContextConfig.executionContext
import nl.ing.api.contacting.shared.client.ContactingAPIClient
import nl.ing.api.contacting.trust.rest.feature.permissions.{AuthorisationService, JWTAccountTokenService}
import nl.ing.api.contacting.trust.rest.filter.SessionContextFilter
import nl.ing.api.contacting.trust.rest.service.PermissionsRequestValidatorService
import nl.ing.api.contacting.util.StringDecrypt
import org.jasypt.encryption.StringEncryptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.{Bean, Configuration => SpringConfiguration}
import org.springframework.core.env.Environment
import org.springframework.util.AntPathMatcher

import scala.collection.JavaConverters._

/**
 * @author Ayush Mittal
 */
@SpringConfiguration
class TrustConfiguration {

  @Autowired
  var coreModule: CoreModule = _

  @Autowired
  protected var env: Environment = _

  @Autowired
  implicit var stringEncryptor: StringEncryptor = _

  val matcher = new AntPathMatcher

  @Bean def authorisationService: AuthorisationService =
    new AuthorisationService(new ContactingPermissionsProvider(coreModule))

  @Bean def contactingApiClient: ContactingAPIClient = coreModule.contactingAPIClient

  @Bean def permissionRequestValidator = new PermissionsRequestValidatorService(authorisationService)

  @Bean def jWTokenService: JWTAccountTokenService = {
    new JWTAccountTokenService(StringDecrypt.decrypt(coreModule.appConfig.getString("next-account-token-secret")))
  }

  @Bean def registerSessionContextFilter(accessTokenFilterProperties: AccessTokenFilterProperties): FilterRegistrationBean[_ <: Filter] = {
    val registrationBean = new FilterRegistrationBean[Filter]
    registrationBean.setFilter(new SessionContextFilter()(contactingApiClient,
      jWTokenService, skipIf))
    registrationBean.addUrlPatterns("/*")
    registrationBean.setOrder(accessTokenFilterProperties.getOrder + 1)
    registrationBean
  }

  private def skipIf(req: HttpServletRequest): Boolean = {
    coreModule.env.getRequiredProperty("cc2.context.filter.skip").split(",")
      .exists(pattern => matcher.`match`(pattern, req.getPathInfo))
  }
}
