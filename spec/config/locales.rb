module Locales
  FORMAT = { "en-GB" => { :date => "%d/%m/%Y" },
             "de-CH" => { :date => "%d.%m.%Y" } }

  def self.format_date(date, user)
    l = user.language_locale
    f = FORMAT[l][:date]
    date.strftime(f)
  end
end
