import React, { useState } from 'react'
import PropTypes from 'prop-types'
import cx from 'classnames'
import SwipeableViews from 'react-swipeable-views'
import PageLayout from '../components/PageLayout'
import Stack from '../components/Stack'
import Section from '../components/Section'
import SquareImageGrid from '../components/SquareImageGrid'
import DownloadLink from '../components/DownloadLink'
import ActionButtonGroup from '../components/ActionButtonGroup'
import SquareImage from '../components/SquareImage'
import PropertyTable from '../components/PropertyTable'
import Warning from '../components/Warning'
import Icon, { iconArrowLeft, iconArrowRight, iconStar } from '../components/Icons'

const noop = () => {}

export default function ModelShow({
  model,
  onOrderClick = noop,
  onClickFavorite = noop,
  t,
  isAddButtonEnabled,
  isFavoriteButtonEnabled,
  buttonInfo
}) {
  const [imageIndex, setImageIndex] = useState(0)

  function addToFavoritesClick() {
    onClickFavorite(true)
  }

  function removeFromFavoritesClick() {
    onClickFavorite(false)
  }

  function handleImageSwipe(index) {
    setImageIndex(index)
  }

  function handleImageBulletClick(index) {
    setImageIndex(index)
  }

  function handleLeftArrowClick() {
    setImageIndex(x => (model.images.length + x - 1) % model.images.length)
  }

  function handleRightArrowClick() {
    setImageIndex(x => (x + 1) % model.images.length)
  }

  return (
    <>
      <PageLayout.Header
        title={
          <>
            {model.isFavorited && <Icon icon={iconStar} style={{ marginRight: '8px', marginTop: '-5px' }} />}
            {model.name}
          </>
        }
      />
      {model.images.length > 1 && (
        <div className="slide-button-visibility-controller">
          <div className="position-relative">
            <button
              className="btn btn-secondary slide-button slide-button--previous"
              aria-label={t.previousImage}
              onClick={handleLeftArrowClick}
            >
              <Icon icon={iconArrowLeft} height="35" width="20" />
            </button>
            <button
              className="btn btn-secondary slide-button slide-button--next"
              aria-label={t.nextImage}
              onClick={handleRightArrowClick}
            >
              <Icon icon={iconArrowRight} height="35" width="20" />
            </button>
            <SwipeableViews
              enableMouseEvents={true}
              resistance={true}
              onChangeIndex={handleImageSwipe}
              index={imageIndex}
            >
              {model.images.map((image, i) => {
                return (
                  <SquareImage
                    key={i}
                    imgSrc={image.imageUrl}
                    className="mb-3 pe-none square-container--not-too-high"
                  />
                )
              })}
            </SwipeableViews>
          </div>
          <div className="mb-4 text-center">
            {model.images.map((image, i) => (
              <button
                type="button"
                key={i}
                className={cx('gallery-button', { 'gallery-button--selected': i === imageIndex })}
                onClick={() => handleImageBulletClick(i)}
              ></button>
            ))}
          </div>
        </div>
      )}
      {model.images.length === 1 && (
        <div className="mb-4">
          <SquareImage imgSrc={model.images[0].imageUrl} className="square-container--not-too-high" />
        </div>
      )}

      <Stack space="5">
        <ActionButtonGroup>
          <button type="button" className="btn btn-primary" onClick={onOrderClick} disabled={!isAddButtonEnabled}>
            {t.addItemToCart}
          </button>
          {model.isFavorited ? (
            <button type="button" className="btn btn-secondary" onClick={removeFromFavoritesClick}>
              {t.removeFromFavorites}
            </button>
          ) : (
            <button
              type="button"
              className="btn btn-secondary"
              onClick={addToFavoritesClick}
              disabled={!isFavoriteButtonEnabled}
            >
              {t.addToFavorites}
            </button>
          )}
          {buttonInfo && <Warning>{buttonInfo}</Warning>}
        </ActionButtonGroup>

        {model.description && (
          <Section title={t.description} collapsible>
            <div
              className="fw-bold decorate-links preserve-linebreaks"
              dangerouslySetInnerHTML={{ __html: model.description }}
            ></div>
          </Section>
        )}

        {model.properties.length > 0 && (
          <Section title={t.properties} collapsible>
            <PropertyTable properties={model.properties} />
          </Section>
        )}

        {model.attachments.length > 0 && (
          <Section title={t.documents} collapsible>
            <Stack space="3">
              {model.attachments.map((attachment, i) => (
                <div key={attachment.id}>
                  <DownloadLink href={attachment.attachmentUrl}>{attachment.filename}</DownloadLink>
                </div>
              ))}
            </Stack>
          </Section>
        )}

        {model.recommends && model.recommends.edges && model.recommends.edges.length > 0 && (
          <Section title={t.compatibles} collapsible>
            {getRecommendsGrid(model.recommends)}
          </Section>
        )}
      </Stack>
    </>
  )
}

function getRecommendsGrid(recommends) {
  const list = recommends.edges.map(({ node }) => ({
    id: node.id,
    href: node.href,
    imgSrc: (node.images.find(() => true) || {}).imageUrl,
    caption: node.name,
    isFavorited: node.isFavorited
  }))
  return <SquareImageGrid list={list} />
}

const modelPropTypes = {
  /** id */
  id: PropTypes.string.isRequired,
  name: PropTypes.string.isRequired,
  manufacturer: PropTypes.string,
  description: PropTypes.string,
  isFavorited: PropTypes.bool.isRequired,
  images: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string.isRequired,
      imageUrl: PropTypes.string.isRequired
    })
  ),
  attachments: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string.isRequired,
      filename: PropTypes.string.isRequired,
      attachmentUrl: PropTypes.string.isRequired,
      size: PropTypes.number
    })
  ),
  properties: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string.isRequired,
      key: PropTypes.string.isRequired,
      value: PropTypes.string
    })
  ),
  recommends: PropTypes.shape({
    edges: PropTypes.arrayOf(
      PropTypes.shape({
        node: PropTypes.shape({
          id: PropTypes.string.isRequired,
          name: PropTypes.string.isRequired,
          manufacturer: PropTypes.string,
          images: PropTypes.arrayOf(
            PropTypes.shape({
              id: PropTypes.string.isRequired,
              imageUrl: PropTypes.string.isRequired
            })
          )
        })
      })
    )
  }),
  totalReservableQuantities: PropTypes.arrayOf(
    PropTypes.shape({
      inventoryPool: PropTypes.shape({
        id: PropTypes.string.isRequired
      }),
      quantity: PropTypes.number.isRequired
    })
  ),
  availability: PropTypes.arrayOf(
    PropTypes.shape({
      inventoryPool: PropTypes.shape({
        id: PropTypes.string.isRequired,
        name: PropTypes.string.isRequired
      }),
      dates: PropTypes.arrayOf(
        PropTypes.shape({
          date: PropTypes.string.isRequired,
          quantity: PropTypes.number.isRequired,
          startDateRestriction: PropTypes.string,
          endDateRestriction: PropTypes.any
        })
      )
    })
  ),
  fetchedUntilDate: PropTypes.string
}

ModelShow.propTypes = {
  /** detail data of the model (including availability information) */
  model: PropTypes.shape(modelPropTypes),
  /** callback when the favorite button is clicked, arguments: the requested new state (true = is favorite, false = is not a favorite) */
  onClickFavorite: PropTypes.func.isRequired,
  /** callback when the order button is clicked, arguments: none */
  onOrderClick: PropTypes.func.isRequired,
  /** order panel */
  orderPanelTmp: PropTypes.node,
  /** Translations */
  t: PropTypes.shape({
    description: PropTypes.string.isRequired,
    properties: PropTypes.string.isRequired,
    documents: PropTypes.string.isRequired,
    compatibles: PropTypes.string.isRequired,
    addItemToCart: PropTypes.string.isRequired,
    removeFromFavorites: PropTypes.string.isRequired,
    addToFavorites: PropTypes.string.isRequired
  }).isRequired,
  /** is "add" enabled? */
  isAddButtonEnabled: PropTypes.bool,
  /** is "add to favorites" enabled? */
  isFavoriteButtonEnabled: PropTypes.bool,
  /** info why buttons are disabled */
  buttonInfo: PropTypes.string
}
