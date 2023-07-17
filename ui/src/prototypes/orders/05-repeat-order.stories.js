import React, { useState } from 'react'
import { de } from 'date-fns/locale'
import { addDays, addYears, format, isValid, startOfDay } from 'date-fns'

import ActionButtonGroup from '../../components/ActionButtonGroup'
import ModalDialog from '../../components/ModalDialog'
import ConfirmDialog from '../../components/ConfirmDialog'
import PageLayout from '../../components/PageLayout'
import Section from '../../components/Section'
import Stack from '../../components/Stack'
import DateRangePicker from '../../components/DateRangePicker'
import Warning from '../../components/Warning'
import PageLayoutMock from '../../story-utils/PageLayoutMock'

export default {
  title: 'Prototypes/Orders/Repeat Order',
  parameters: { layout: 'fullscreen' }
}

const formatDate = date => format(date, 'P', { locale: de })
//const parseDate = s => parse(s, 'P', new Date(), { locale: de })

const today = startOfDay(new Date())
const durationDays = 2
const maxDate = addYears(today, 10)

export function repeatOrder() {
  const [step, setStep] = useState('')
  const [withOptions, setWithSpecialCases] = useState(false)

  const [selectedRange, setSelectedRange] = useState({ startDate: today, endDate: addDays(today, durationDays - 1) })
  const [validationResult, setValidationResult] = useState({ isValid: true })

  function openFormDialog() {
    setStep('form')
  }
  function openOkDialog() {
    setStep('ok')
  }
  function gotToCart() {
    setStep('cart')
  }
  function goToInitialStep() {
    setStep('')
  }

  function validateDates({ startDate, endDate }) {
    if (!isValid(startDate) || !isValid(endDate)) {
      return { isValid: false } // (no message required since fields are marked invalid)
    }
    if (startDate > endDate) {
      return { isValid: false, dateMessage: 'Enddatum muss nach Beginndatum sein' }
    }
    if (startDate < today) {
      return { isValid: false, dateMessage: 'Datum liegt in der Vergangenheit' }
    }
    if (endDate > maxDate) {
      return { isValid: false, dateMessage: `Datum darf nicht nach ${formatDate(maxDate)} sein` }
    }
    return { isValid: true }
  }

  function changeSelectedRange(range) {
    setSelectedRange(range)
    setValidationResult(validateDates(range))
  }

  return (
    <PageLayoutMock>
      {step === 'cart' ? (
        <>
          <PageLayout.Header title="Warenkorb"></PageLayout.Header>
          <p className="text-muted">
            <p>...</p>
            <p>...</p>
            <button className="btn btn-light btn-sm" onClick={goToInitialStep}>
              Restart story
            </button>
          </p>
        </>
      ) : (
        <>
          <p className="text-muted">Interactive story which shows how to repeat an order.</p>
          <p className="text-muted mb-5">
            <label>
              <input type="checkbox" checked={withOptions} onChange={e => setWithSpecialCases(e.target.checked)} />{' '}
              Order with options
            </label>
          </p>
          <PageLayout.Header
            title="Video Semesterprojekt"
            subTitle="Zwischen 26.5.2020 und 7.6.2020, 8 Gegenstände"
          ></PageLayout.Header>
          <Stack space={5}>
            <Section title="Status" collapsible>
              <Stack space="3">
                <div>...</div>
                <ActionButtonGroup>
                  <button type="button" className="btn btn-secondary" onClick={openFormDialog}>
                    Bestellung wiederholen
                  </button>
                </ActionButtonGroup>
              </Stack>
            </Section>
            <Section title="Zweck">...</Section>
            <Section title="Gegenstände">...</Section>
          </Stack>
        </>
      )}

      <div>&nbsp;</div>
      <div>&nbsp;</div>

      {step === 'form' && (
        <ModalDialog
          title="Gegenstände hinzufügen"
          className="ui-repeat-order-dialog"
          shown
          dismissible
          onDismiss={goToInitialStep}
        >
          <ModalDialog.Body>
            <form noValidate autoComplete="off" id="repeat-order-form">
              <Stack space={4}>
                <Section>
                  <p>{withOptions ? 5 : 8} Gegenstände werden zum Warenkorb hinzugefügt.</p>
                  {withOptions && (
                    <>
                      <Warning className="fs-2">
                        Hinweis: 3 Optionen können nur durch die Verleihstelle hinzugefügt werden.
                      </Warning>
                    </>
                  )}
                </Section>
                <Section title="Bestellung für" collapsible>
                  Anna Beispiel (persönlich)
                </Section>
                <Section title="Zeitraum" collapsible>
                  <fieldset>
                    <legend className="visually-hidden">Zeitraum</legend>
                    <div className="d-flex flex-column gap-3">
                      <DateRangePicker
                        locale={de}
                        selectedRange={selectedRange}
                        onChange={changeSelectedRange}
                        minDate={today}
                        maxDate={maxDate}
                      />
                      {validationResult.dateMessage && <Warning>{validationResult.dateMessage}</Warning>}
                    </div>
                  </fieldset>
                </Section>
              </Stack>
            </form>
          </ModalDialog.Body>
          <ModalDialog.Footer>
            <button
              type="submit"
              className="btn btn-primary"
              form="repeat-order-form"
              onClick={openOkDialog}
              disabled={!validationResult.isValid}
            >
              Hinzufügen
            </button>
            <button type="button" className="btn btn-secondary" onClick={goToInitialStep}>
              Abbrechen
            </button>
          </ModalDialog.Footer>
        </ModalDialog>
      )}

      {step === 'ok' && (
        <ConfirmDialog title="Gegenstände hinzugefügt" shown onConfirm={gotToCart} confirmLabel="Zum Warenkorb">
          <p>{withOptions ? 5 : 8} Gegenstände wurden in den Warenkorb gelegt und können dort überprüft werden.</p>
        </ConfirmDialog>
      )}
    </PageLayoutMock>
  )
}
