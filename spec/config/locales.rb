module Locales
  FORMAT = {"en-GB" => {date: "%d/%m/%Y"},
            "de-CH" => {date: "%d.%m.%Y"}}
  FORMAT_SHORT = {"en-GB" => {date: "%d/%m/%y"},
                  "de-CH" => {date: "%d.%m.%y"}}
  WEEKDAYS_SHORT = {"en-GB" => %w[Sun Mon Tue Wed Thu Fri Sat],
                    "de-CH" => %w[So. Mo. Di. Mi. Do. Fr. Sa.]}

  def self.format_date(date, user)
    l = user.language_locale
    f = FORMAT[l][:date]
    date.strftime(f)
  end

  def self.format_date_short(date, user)
    l = user.language_locale
    f = FORMAT_SHORT[l][:date]
    date.strftime(f)
  end

  def self.format_date_with_weekday(date, user)
    l = user.language_locale
    "#{WEEKDAYS_SHORT[l][date.wday]}, #{format_date(date, user)}"
  end
end
