## 为什么API管理平台必须使用IAM？



APIs are key components in any digital transformation journey. They enable organizations to create new business models and connect with partners and customers, while also providing seamless digital experiences by linking systems and services together. In today’s API economy, all modern architecture concepts deeply rely on APIs.

API是任何数字化转型过程中的关键组件。它们使组织能够创建新的商业模式并与合作伙伴和客户建立联系，同时通过将系统和服务链接在一起来提供无缝的数字体验。 在当今的API经济中，所有现代架构概念都深深地依赖于API

Access delegation is the primary security requirement in an API ecosystem, where someone else will access an API on your behalf and you need to delegate the corresponding access rights to the application or service. Providing end-users credentials or the usage of an API key is not a recommended approach anymore. OAuth 2.0 has become the norm for access delegation. When using OAuth 2.0, the access token is shared with a third party with limited access privileges and an expiry time. Along with OAuth 2.0, OIDC emerged as the authentication layer in API management platforms.

访问委托是API生态系统中的主要安全要求，其他人将代表您访问API，并且您需要将相应的访问权限委派给应用程序或服务。提供最终用户的密钥和API密码已经不在是推荐的方式。OAuth 2.0已经成为访问委托的规范。使用OAuth 2.0时，访问令牌（权限和有效时间受限）将与第三方共享。OIDC和OAuth 2.0一起负责API管理平台的身份认证。



By nature, when the digital industry moves, hackers follow. So, with APIs becoming industry norms,hackers have sought out new ways to breach API ecosystems. The nature and sheer amount of data exposed, coupled with the exponentially high usage of APIs, increase the attack surface and the damage. Exposing sensitive data—e.g., personally identifiable information (PII)—cost companies millions of dollars in reparations and result in PR nightmares. For example, Facebook was fined £500,000 by the UK's data protection watchdog for its role in the Cambridge Analytica data scandal. And, two security breaches, which exposed the personal information of more than 1 billion users, cost Yahoo $350 million.

从本质上讲，随着数字产业的发展，黑客也会随之而来。 因此，随着API成为行业规范，黑客已经寻求突破API生态系统的新方法。 暴露的数据的性质和数量，加上API的指数级使用，增加了攻击面和破坏力。 公开敏感数据（例如，个人身份信息（PII））使公司损失了数百万美元的赔偿，并导致公关恶梦。 例如，Facebook因其在Cambridge Analytica数据丑闻中的作用而被英国数据保护监管机构罚款500,000英镑。 雅虎因两次安全漏洞暴露了超过10亿用户的个人信息损失了3.5亿美元。



In a typical API management platform, the key manager component or authorization server mainly focuses on access delegation or securely managing access tokens. However, API security goes beyond simple authorization capabilities; refer to the Open Web Application Security Project’s (OWASP) recent API Security Top 10 for more details on this. This is why we need Identity and access management (IAM) solutions in API platforms to fill this security gap. IAM solution not only strengthens security but also brings additional capabilities to enhance digital transformation efforts. The following are IAM capabilities that are essential in your API management platform.

在典型的API管理平台中，密钥管理组件或授权服务器主要侧重于访问委托或安全地管理访问令牌。 但是，API安全性超出了简单的授权功能。 有关更多详细信息，请参考开放式Web应用程序安全性项目（OWASP）的最新API安全性Top 10。 这就是为什么我们需要API平台中的身份和访问管理（IAM）解决方案来填补此安全漏洞的原因。 IAM解决方案不仅可以增强安全性，还可以带来其他功能以增强数字转型的工作。以下IAM功能在你的API管理平台中必不可少。

- Extended Access Delegation Capabilities
- 扩展访问委托功能
- End-User Identity Management
- 最终用户身份管理
- Strong and Adaptive Authentication
- 强大的自适应身份认证
- Cross Protocol Single Sign-On / Sign Out
- 跨协议单点登录/注销
- Identity Federation and Social Login
- 身份联邦和社交账号登录
- Enforce Authorization
- 强制授权
- Privacy Management
- 隐私管理

### 扩展访问委托功能

OAuth 2.0核心规范定义了四种主要的授权类型：授权码，客户端凭据，设备码和令牌刷新。但是OAuth2.0框架支持扩展以支持更高级别的授权场景，比如SAML 2.0或Kerberos OAuth2的授权。在基于SAML 2.0的授权中，如果已经登录支持SAML协议的应用，可以将相同的SAML 2.0 令牌交换为OAuth2.0 的令牌，而这个过程不影响用户体验，不会提示客户重新授权。如果是在Windows环境中，则可以使用相同的Windows登录凭据并使用Kerberos令牌获取OAuth访问令牌。如果只是一个基本的API管理平台，基本的授权委托已经可以满足要求。但是API管理组件是应用程序的核心，因此需要支持扩展的需求，将API管理平台和IAM解决方案集成可以提供高级的访问委托功能。OAuth 2.0 只提供访问授权但是不显示用户身份。如果需要用户身份，可以使用基于OAuth 2.0的OIDC，OIDC通过访问令牌和一个JWT类型的令牌提供用户信息，这是IAM解决方案的主要功能之一。

### 最终用户身份管理

API管理平台可以在组织内部或外部使用。不论哪种情况，API都将会被人，设备或万物使用。因此，管理数字身份成为任何API管理平台的重点。解决方案必须处理不同的身份类型：可能是人或设备又或者是人和设备都可以访问相同的系统。访问系统的身份数量可能从数千到数百万不等。这些数字身份通常存储在异构身份存储中。

此外，用户如何更改忘记的密码，如何定义密码强度，最终用户可用来恢复其凭据的机制以及管理员是否可以（经同意）访问用户帐户是所有系统在处理身份认证时必须解决的一些典型问题。

身份管理十分的复杂，但是IAM解决方案可以有效性的管理这些复杂性。身份可以以不同的形式存在于多个身份存储中，也许一个身份可以分布在多个身份存储中。这些只是身份管理带来的众多担忧中的一小部分。在API管理平台的初始阶段，你可能不会遇到这些问题，但是从一开始就拥有正确的IAM系统可以确保您的平台已准备好应对未来的挑战。

### Strong and Adaptive Authentication

Within the context of API security, often, we focus heavily on access delegation or securely managing access tokens; this hinders the importance of end-user authentication. Even now, username and password-based authentication is the most commonly used authentication mechanism. While it may be convenient, it is also the least secure authentication mechanism.

Multi-factor authentication (MFA) emerged as an answer to this problem. It implements a layered defense and made it more difficult for an unauthorized person to access a target, such as a physical location, computing device, web service, network, or database. The MFA concept is based on the assumption that if one factor is compromised or broken, an attacker still has at least one more barrier to breach before successfully breaking into the target. Therefore, it’s more secure. Authentication factors in MFA rely on two or more independent credentials of three categories.

* Knowledge factors — Things only the user knows, such as passwords

* Possession factors — Things only the user has, such as ATM cards

* Inherence factors — Things only the user is, such as a fingerprint

The level of security provided by MFA has made it the best way to authenticate in the modern world. Even if one of the factors is compromised by an attacker, it is highly unlikely that all the other factors can also become compromised.

Conversely, even though MFA provides high security, it hinders usability. A static authentication flow is not convenient for different sets of users. To counter, adaptive authentication brings in the ability to switch the authentication flow based on the context. This shouldn’t be misunderstood as a completely different mechanism that replaces MFA. Adaptive authentication orchestrates different authenticators based on the context during the user authentication process. The best part is that most times users won’t even know that the authentication process has changed. Adaptive authentication intelligently takes in various factors in the current authentication process context and provides the authentication flow to the user.

### Cross Protocol Single Sign-On / Sign Out

At a glance, we can say that the primary focus of the API management platform is to securely manage APIs exposed in the system. But, in a complete digital transformation project, API integration is just a fraction and there are multiple applications that consumers want to access to perform a given business use case. Single sign-on (SSO) is the mechanism that ensures customers
have a consistent login experience with common credentials across different digital properties

Few authorization servers may support OIDC-based single sign-on. Even though modern applications support OIDC-based federation, most applications widely use SAML (and there are applications that use Ws-Federation as well). In most digital transformations projects, we see most of these protocols are being used in applications; therefore, you have to pick an IAM solution that supports all of these federation protocols. The solution should especially support cross protocols
single sign-on along with sign out.

If a platform contains legacy applications that use proprietary protocols for federation, then, your IAM solution should have the capability to extend its federation authentication support for these non-standardized protocols as well.

### Identity Federation and Social Login

A sophisticated API management platform should attract developers to interact with it. And, a thriving
developer community is a sign of success for a platform.One indicator to judge an APIM platform’s
success is the developer community around it. Developers commonly use Git, and, almost all have at
least one social account, such as Facebook, Twitter, and LinkedIn. Allowing developers to use their
social accounts to log in to the platform will attract more developers.

Even if you use API management platforms internally, you may have different business (BU) units,
where employees reside in BU-specific identity stores and want to access the API platform. In this
particular scenario, we can use identity federation to let these internal users seamlessly access the
new platform. Even when an organization expands, e.g., following acquisitions and mergers, we can
simply use identity federation to simplify complex identity integration needs and onboard new users
in a few minutes.

### Enforce authorization

Authentication verifies the identity of a person or device with the authorization to see whether that verified identity can perform the given action or access the data. In other words, authorization ensures users or things can only access what they are authorized to access. To implement authorization in an API we need to consider two levels of authorization. First, we need to validate the authorization in the API entry point to see whether this user can execute the given action. Then, we need to verify whether the user can access the exposed data.

身份验证通过授权验证用户或设备的身份，以查看该已验证身份是否可以执行给定的操作或访问数据。换而言之，授权可确保用户或设备只能访问其有权访问的内容。要对API实现授权，我们需要考虑两个级别的授权。首先，我们需要在API入口点中验证授权，以查看该用户是否可以执行给定的操作。然后，我们需要验证用户是否可以访问公开的数据。



The OAuth 2.0 scope is the best choice to enforce resource-level authorization. This can either be validated in the backend service level or in the API gateway level, which is the recommended clean approach. If we look at a basic OAuth 2.0 request flow scope validation can be done in two main places. In the authentication request flow before granting the access, the scopes need to be
validated and check if authenticated users are eligible to grant the requested scope or not. Then, when it comes to API invocation, there should be another validation to check whether the API is accessible with the given scope. In the initial authorization validation, if you are using an IAM solution over basic key management components, it is possible to use more fine-grained
authorization using XACML or an open policy agent (OPA).



Then we need to validate the authorization in the API implementation level to prevent unauthorized access to sensitive data. Even though a user is capable of invoking a given API action, he or she may not access the sensitive data. This should be validated in the code level where we need to pass the user information. This is another use case of JWT tokens, where authenticated user information can be passed into downstream services with the JWT tokens.

### Privacy management隐私管理

A sophisticated API management platform should attract developers to interact with it. And, a thriving
developer community is a sign of success for a platform.One indicator to judge an APIM platform’s
success is the developer community around it. Developers commonly use Git, and, almost all have at
least one social account, such as Facebook, Twitter, and LinkedIn. Allowing developers to use their
social accounts to log in to the platform will attract more developers.

一个完善的API管理平台应当能够吸引开发者与其交互，而且一个蓬勃发展的开发者社区是平台成功的一个标志。判断一个API管理平台成功的指标之一便是围绕它的开发者社区。开发者通常使用Git，而且至少拥有一个社交帐号，比如Facebook，Twitter和LinkedIn。允许开发者使用社交账号登录API管理平台将会吸引更多的开发者。



Even if you use API management platforms internally, you may have different business (BU) units,
where employees reside in BU-specific identity stores and want to access the API platform. In this
particular scenario, we can use identity federation to let these internal users seamlessly access the
new platform. Even when an organization expands, e.g., following acquisitions and mergers, we can
simply use identity federation to simplify complex identity integration needs and onboard new users
in a few minutes.

即使在内部使用API管理平台，也可能有多个业务部门，业务部门的员工希望使用原来的账号登录API管理平台。对于这种特殊场景，可以使用身份联邦允许内部用户无缝的访问新平台。即使组织的规模不断扩大，例如在收购和兼并之后，可以使用身份联邦简化身份继承并在几分钟之内加入新的用户。



即使组织规模不断扩大，例如在收购和兼并之后，我们也可以
只需使用身份联合来简化复杂的身份集成需求并加入新用户
在几分钟内。

### Conclusion

APIs are the main element in any digital transformation journey. This exponential
increase in API adoption has made it a prime target for hackers. Hence it’s important
to correctly implement API security correctly. API platforms differ from one to another,
hence, API security infrastructure should be designed to cater to unique requirements
in each API platform and its sensitivity.



In a growing API platform, consumers are the main asset, and identity management is
foundational. API consumers require a seamless onboarding and login experience
along with enhanced security. Trust and privacy is a key expectation from both
regulatory authorities and consumers. Hence, as I discussed in this article, coupling
your API management solution with an enterprise Identity and access management
solution is a key requirement now more than ever.