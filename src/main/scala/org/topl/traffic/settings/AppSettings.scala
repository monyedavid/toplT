package org.topl.traffic.settings

final case class AppSettings(
  service: ServiceSettings,
  default: DefaultSettings
)

object AppSettings extends SettingCompanion[AppSettings]
