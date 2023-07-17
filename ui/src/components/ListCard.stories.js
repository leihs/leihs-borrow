import React from 'react'
import { action } from '@storybook/addon-actions'
import ListCard from './ListCard'
import SquareImage from './SquareImage'
import Badge from './Badge'

export default {
  title: 'Design Components/Content/ListCard',
  component: ListCard,
  args: {
    onItemClick: action('on item click')
  }
}

const imgSrc = require('../../static/example-images/categories/2b4c8bd3-3d65-5e68-bf7a-3649ec67a1a2.jpg')

export const listCard = () => {
  return (
    <div>
      <h1>ListCard</h1>
      <p className="text-muted">Presents a list entry, often linked to an action (href or onClick).</p>
      <p className="text-muted">
        A <code>ListCard.Stack</code> can be used to wrap multiple cards with dividers and space.
      </p>
      <p className="text-muted">The card can be assembled with three sub-components:</p>
      <ul className="text-muted">
        <li>
          <code>ListCard.Title</code>
          <br />A one liner, default font, with a tiny gap to the next element
        </li>
        <li>
          <code>ListCard.Body</code>
          <br />
          Arbitrary content, small font
        </li>
        <li>
          <code>ListCard.Foot</code>
          <br />
          Arbitrary content, with a gap to the previous element
        </li>
      </ul>
      <ListCard.Stack>
        <ListCard>
          <ListCard.Title>Ibex</ListCard.Title>
          <ListCard.Body>
            The Alpine ibex (Capra ibex), also known as the steinbock, bouquetin, or simply ibex, is a species of wild
            goat that lives in the mountains of the European Alps.
          </ListCard.Body>
          <ListCard.Foot className="very-small">{getFoot('Bovidae')}</ListCard.Foot>
        </ListCard>
        <ListCard href="#fake-link-2" foot={getFoot('Sciuridae')}>
          <ListCard.Title>Marmot</ListCard.Title>
          <ListCard.Body>
            Marmots are relatively large ground squirrels in the genus Marmota, with 15 species living in Asia, Europe,
            and North America.
          </ListCard.Body>
          <ListCard.Foot className="very-small">{getFoot('Sciuridae')}</ListCard.Foot>
        </ListCard>
        <ListCard href="#fake-link-3">
          <ListCard.Title>Ptarmigan</ListCard.Title>
          <ListCard.Body>
            The rock ptarmigan (Lagopus muta) is a medium-sized game bird in the grouse family
          </ListCard.Body>
          <ListCard.Foot className="very-small">{getFoot('Phasianidae')}</ListCard.Foot>
        </ListCard>
      </ListCard.Stack>
    </div>
  )
}
listCard.storyName = 'ListCard'

export const minimalExample = () => {
  return (
    <div>
      <h1>ListCard</h1>
      <p className="text-muted">Minimal example with unstructured content</p>
      <ListCard.Stack>
        <ListCard>Ibex</ListCard>
        <ListCard>Marmot</ListCard>
        <ListCard>Ptarmigan</ListCard>
      </ListCard.Stack>
    </div>
  )
}

export const with_onclick = () => {
  return (
    <div>
      <h1>ListCard</h1>
      <p className="text-muted">
        Using <code>onClick</code> instead of <code>href</code> prop.
      </p>
      <ListCard.Stack>
        <ListCard onClick={action('list-card-click-1')}>Ibex</ListCard>
        <ListCard onClick={action('list-card-click-2')}>Marmot</ListCard>
        <ListCard onClick={action('list-card-click-3')}>Ptarmigan</ListCard>
      </ListCard.Stack>
    </div>
  )
}
with_onclick.storyName = 'Using onClick'

export const withImage = () => {
  return (
    <div>
      <h1>ListCard</h1>
      <p className="text-muted">With image</p>
      <ListCard.Stack>
        <ListCard img={<SquareImage imgSrc={imgSrc} />}>
          <ListCard.Title>Ibex</ListCard.Title>
          <ListCard.Body>
            The Alpine ibex (Capra ibex), also known as the steinbock, bouquetin, or simply ibex, is a species of wild
            goat that lives in the mountains of the European Alps.
          </ListCard.Body>
          <ListCard.Foot>{getFoot('Bovidae')}</ListCard.Foot>
        </ListCard>
        <ListCard img={<SquareImage />} href="#fake-link-2" foot={getFoot('Sciuridae')}>
          <ListCard.Title>Marmot</ListCard.Title>
          <ListCard.Body>
            Marmots are relatively large ground squirrels in the genus Marmota, with 15 species living in Asia, Europe,
            and North America.
          </ListCard.Body>
          <ListCard.Foot>{getFoot('Sciuridae')}</ListCard.Foot>
        </ListCard>
        <ListCard href="#fake-link-3" img={<SquareImage imgSrc={imgSrc} />}>
          <ListCard.Title>Ptarmigan</ListCard.Title>
          <ListCard.Foot>{getFoot('Phasianidae')}</ListCard.Foot>
        </ListCard>
      </ListCard.Stack>
    </div>
  )
}

export const oneLineLink = () => {
  return (
    <div>
      <h1>ListCard</h1>
      <div className="d-flex flex-column gap-4">
        <div>
          <p className="text-muted">
            By default the geometry of linked card assumes content with two lines (or more). To keep the heights evenly
            distributed this is not changed for cards which have one line only:
          </p>
          <ListCard.Stack>
            <ListCard onClick={() => {}}>
              <ListCard.Title>Something</ListCard.Title>
              <ListCard.Body>With two lines</ListCard.Body>
            </ListCard>
            <ListCard onClick={() => {}}>
              <ListCard.Title>A One Liner</ListCard.Title>
            </ListCard>
            <ListCard onClick={() => {}}>
              <ListCard.Title>Another thing</ListCard.Title>
              <ListCard.Body>With two lines</ListCard.Body>
            </ListCard>
          </ListCard.Stack>
        </div>
        <div>
          <p className="text-muted">
            However when most or all cards have only one line, a lot of whitespace and also some assymetry results:
          </p>
          <ListCard.Stack>
            <ListCard onClick={() => {}}>
              <ListCard.Title>Ibex</ListCard.Title>
            </ListCard>
            <ListCard onClick={() => {}}>
              <ListCard.Title>Marmot</ListCard.Title>
            </ListCard>
            <ListCard onClick={() => {}}>
              <ListCard.Title>Ptarmigan</ListCard.Title>
            </ListCard>
          </ListCard.Stack>
        </div>
        <div>
          <p className="text-muted">
            For a more compact look, apply the <code>`oneLine`</code> prop:
          </p>
          <ListCard.Stack>
            <ListCard onClick={() => {}} oneLine>
              <ListCard.Title>Ibex</ListCard.Title>
            </ListCard>
            <ListCard onClick={() => {}} oneLine>
              <ListCard.Title>Marmot</ListCard.Title>
            </ListCard>
            <ListCard onClick={() => {}} oneLine>
              <ListCard.Title>Ptarmigan</ListCard.Title>
            </ListCard>
          </ListCard.Stack>
        </div>
        <div></div>
      </div>
    </div>
  )
}

export const edgeCases = () => {
  return (
    <div>
      <h1>ListCard</h1>
      <div className="d-flex flex-column gap-4" style={{ maxWidth: '25rem' }}>
        <div>
          <p className="text-muted">Examples with long title:</p>
          <ListCard.Stack>
            <ListCard>
              <ListCard.Title>
                Lorem ipsum dolor sit, amet consectetur adipisicing elit. Libero minus debitis labore
              </ListCard.Title>
            </ListCard>
            <ListCard onClick={() => {}}>
              <ListCard.Title>
                Lorem ipsum dolor sit, amet consectetur adipisicing elit. Libero minus debitis labore
              </ListCard.Title>
            </ListCard>
            <ListCard onClick={() => {}}>
              <ListCard.Title>LoremipsumdolorsitametconsecteturadipisicingelitLiberominusdebitislabore</ListCard.Title>
            </ListCard>
          </ListCard.Stack>
        </div>

        <div></div>
      </div>
    </div>
  )
}

export const flexFoot = ({ onItemClick }) => {
  return (
    <div>
      <h1>ListCard</h1>
      <p className="text-muted">
        Example where the footer is arranged in 2nd column for screen size md+ (make sure to remove the top padding the
        footer has by default)
      </p>
      <ListCard.Stack>
        <ListCard onClick={onItemClick}>
          <div className="d-md-flex">
            <div style={{ flex: '1 1 52%' }} className="pe-4">
              <ListCard.Title>Ibex</ListCard.Title>
              <ListCard.Body>
                The Alpine ibex (Capra ibex), also known as the steinbock, bouquetin, or simply ibex, is a species of
                wild goat that lives in the mountains of the European Alps.
              </ListCard.Body>
            </div>
            <div style={{ flex: '1 1 48%' }}>
              <ListCard.Foot className="very-small p-md-0">{getFoot('Bovidae')}</ListCard.Foot>
            </div>
          </div>
        </ListCard>
      </ListCard.Stack>
    </div>
  )
}

export const restProps = ({ onItemClick }) => {
  return (
    <div>
      <h1>ListCard</h1>
      <p className="text-muted">Set arbitrary attributes with restProps</p>
      <ListCard className="border border-primary text-primary p-2" onClick={onItemClick}>
        <ListCard.Title>Ibex</ListCard.Title>
        <ListCard.Body>
          The Alpine ibex (Capra ibex), also known as the steinbock, bouquetin, or simply ibex, is a species of wild
          goat that lives in the mountains of the European Alps.
        </ListCard.Body>
        <ListCard.Foot className="very-small">{getFoot('Bovidae')}</ListCard.Foot>
      </ListCard>
    </div>
  )
}

function getFoot(family) {
  return <Badge>Family: {family}</Badge>
}
